// Copyright 1998,1999,2000,2001,2018, Henrik Lauritzen.
/*
    This file is part of the Hojo interpreter & toolkit.

    The Hojo interpreter & toolkit is free software: you can redistribute it
    and/or modify it under the terms of the GNU Affero General Public License
    as published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    The Hojo interpreter & toolkit is distributed in the hope that it will
    be useful or (at least have historical interest),
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
    See the GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this file.  If not, see <http://www.gnu.org/licenses/>.
*/
package org.xodonex.util.sound.midi;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.SysexMessage;
import javax.sound.midi.Track;

import org.xodonex.util.StringUtils;

/**
 *
 * @author Henrik Lauritzen
 */
public class MidiCodec implements MidiTrackFilter, MidiEventFilter {

    private final static Pattern P_SPLIT = Pattern.compile("[\000- ]+");

    private final static String CODE_COMMENT = "#";
    private final static String CODE_FORMAT = "Fmt/Div";
    private final static String CODE_PPQ = "PPQ";
    private final static String CODE_SMPTE_24 = "SMPTE_24";
    private final static String CODE_SMPTE_25 = "SMPTE_25";
    private final static String CODE_SMPTE_30 = "SMPTE_30";
    private final static String CODE_SMPTE_30DROP = "SMPTE_30DROP";
    private final static String CODE_TRACK = "Trk";
    private final static String CODE_META = "M";
    private final static String CODE_SYSEX = "S";
    private final static String CODE_CHN = "C";

    // decoding target
    private PrintWriter _out;

    // cached data
    private String _trackInfo = null;
    private int _fileType = 1;

    public MidiCodec() {
        this((Writer)null);
    }

    public MidiCodec(OutputStream decodeTarget) {
        this(decodeTarget == null ? null
                : new OutputStreamWriter(decodeTarget));
    }

    public MidiCodec(Writer decodeTarget) {
        if (decodeTarget == null) {
            _out = null;
        }
        else if (!(decodeTarget instanceof PrintWriter)) {
            _out = new PrintWriter(decodeTarget);
        }
        else {
            _out = (PrintWriter)decodeTarget;
        }
    }

    public PrintWriter getTarget() {
        return _out;
    }

    public int getPreferredFileType() {
        return _fileType;
    }

    public Sequence encodeSequence(String s)
            throws IllegalArgumentException {
        return encode(parse(s));
    }

    public Sequence encodeSequence(BufferedReader r)
            throws IOException, IllegalArgumentException {
        return encode(parse(r));
    }

    public Sequence encodeSequence(File f)
            throws IOException, IllegalArgumentException {
        BufferedReader r = new BufferedReader(new FileReader(f));

        try {
            return encode(parse(r));
        }
        finally {
            r.close();
        }
    }

    public void decodeSequence(File f) throws IOException {
        try {
            _fileType = MidiSystem.getMidiFileFormat(f).getType();
            decodeSequence(MidiSystem.getSequence(f));
        }
        catch (InvalidMidiDataException e) {
            throw new IOException(e.getMessage());
        }

    }

    public void decodeSequence(Sequence s) {
        _out.println("# MIDI sequence decoded by MidiCodec");
        _out.println();
        _out.print(CODE_FORMAT + "  " + (_fileType & 3) + " ");

        float div = s.getDivisionType();
        String code;

        if (div == Sequence.PPQ) {
            code = CODE_PPQ;
        }
        else if (div == Sequence.SMPTE_25) {
            code = CODE_SMPTE_25;
        }
        else if (div == Sequence.SMPTE_30) {
            code = CODE_SMPTE_30;
        }
        else if (div == Sequence.SMPTE_30DROP) {
            code = CODE_SMPTE_30DROP;
        }
        else {
            code = CODE_SMPTE_24;
        }
        _out.println("  " + code + " " +
                StringUtils.expandLeft(Integer.toString(s.getResolution()), '0',
                        10));

        Track[] tracks = s.getTracks();
        for (int i = 0; i < tracks.length; i++) {
            _trackInfo = Integer.toString(i + 1);
            decodeTrack(tracks[i]);
        }

        _out.flush();
    }

    public void decodeTrack(Track t) {
        if (_out != null) {
            _out.println("\n" + CODE_TRACK + " "
                    + (_trackInfo == null ? "" : _trackInfo));
        }

        for (int i = 0, max = t.size(); i < max; i++) {
            filter(t.get(i));
        }
    }

    public static void decodeMeta(long tick, MetaMessage msg, PrintWriter out) {
        out.print(StringUtils.expandLeft(Long.toString(tick), ' ', 10) + "   ");
        out.println(CODE_META + " " + StringUtils.expandLeft(Integer.toString(
                msg.getType(), 16), '0', 2) + "  \"" +
                StringUtils.toJavaString(new String(msg.getData())) + "\"");
    }

    public static void decodeSysex(long tick, SysexMessage msg,
            PrintWriter out) {
        out.print(StringUtils.expandLeft(Long.toString(tick), ' ', 10) + "   ");
        out.println(CODE_SYSEX + "  \"" +
                StringUtils.toJavaString(new String(msg.getData())) + "\"");
    }

    public static void decodeShort(long tick, ShortMessage msg,
            PrintWriter out) {
        out.print(StringUtils.expandLeft(Long.toString(tick), ' ', 10) + "   ");
        out.println(CODE_CHN + " " + StringUtils.expandLeft(Integer.toString(
                msg.getChannel(), 16), '0', 2) + "  " +
                StringUtils.expandLeft(Integer.toString(msg.getCommand(), 16),
                        '0', 2)
                + " " +
                StringUtils.expandLeft(Integer.toString(msg.getData1(), 16),
                        '0', 2)
                + " " +
                StringUtils.expandLeft(Integer.toString(msg.getData2(), 16),
                        '0', 2));
    }

    protected boolean filterTrack(Track t) {
        return true;
    }

    protected boolean filterMeta(MidiEvent e, MetaMessage msg) {
        return true;
    }

    protected boolean filterSysex(MidiEvent e, SysexMessage msg) {
        return true;
    }

    protected boolean filterShort(MidiEvent e, ShortMessage msg) {
        return true;
    }

    @Override
    public final void filter(Sequence s, Track t) {
        if (!filterTrack(t)) {
            return;
        }

        decodeTrack(t);
    }

    @Override
    public final boolean filter(MidiEvent e) {
        MidiMessage m = e.getMessage();

        if (m instanceof MetaMessage) {
            MetaMessage mmsg = (MetaMessage)m;

            if (!filterMeta(e, mmsg)) {
                return false;
            }
            if (_out != null) {
                decodeMeta(e.getTick(), mmsg, _out);
            }
        }
        else if (m instanceof SysexMessage) {
            SysexMessage symsg = (SysexMessage)m;

            if (!filterSysex(e, symsg)) {
                return false;
            }
            if (_out != null) {
                decodeSysex(e.getTick(), symsg, _out);
            }
        }
        else {
            ShortMessage smsg = (ShortMessage)m;

            if (!filterShort(e, smsg)) {
                return false;
            }
            if (_out != null) {
                decodeShort(e.getTick(), smsg, _out);
            }
        }

        return true;
    }

    // parse decoded MIDI into IR representation
    private Command parseLine(String in) throws IllegalArgumentException {
        in = in.trim();
        if (in.length() == 0 || in.startsWith(CODE_COMMENT)) {
            return null;
        }

        if (Character.isDigit(in.charAt(0))) {
            // event command (starts with tick)
            String[] tmp = P_SPLIT.split(in, 2);

            long tick = Long.parseLong(tmp[0]);
            String s = tmp[1].trim();

            if (s.startsWith(CODE_SYSEX)) {
                return new SysexEventCommand(s, tick);
            }
            else if (s.startsWith(CODE_META)) {
                return new MetaEventCommand(s, tick);
            }
            else if (s.startsWith(CODE_CHN)) {
                return new ChannelEventCommand(s, tick);
            }
            else {
                throw new IllegalArgumentException(in);
            }
        }
        else {
            if (in.startsWith(CODE_FORMAT)) {
                return new FmtCode(in);
            }
            else if (in.startsWith(CODE_TRACK)) {
                return new TrackCode(in);
            }
            else {
                throw new IllegalArgumentException(in);
            }
        }
    }

    // read a MIDI encoding and parse it into IR
    private List parse(String s) throws IllegalArgumentException {
        try {
            return parse(new BufferedReader(new StringReader(s)));
        }
        catch (IOException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    private List parse(BufferedReader in) throws IOException {
        List result = new ArrayList();
        Command item;
        String line;

        while (true) {
            line = in.readLine();
            if (line == null) {
                break;
            }

            item = parseLine(line);
            if (item != null) {
                result.add(item);
            }
        }

        return result;
    }

    // encode a MIDI sequence from a parsed IR
    private Sequence encode(List l) throws IllegalArgumentException {
        // <yuck>
        _trackInfo = null;
        _fileType = 1;
        // </yuck>

        try {
            Sequence s = new Sequence(Sequence.PPQ, 240);
            List ts = new ArrayList(16);

            for (Iterator i = l.iterator(); i.hasNext();) {
                Command c = (Command)i.next();
                s = c.execute(s, ts);
            }

            return s;
        }
        catch (Exception e) {
            if (e instanceof IllegalArgumentException) {
                throw (IllegalArgumentException)e;
            }
            else {
                throw new IllegalArgumentException(e.getMessage());
            }
        }
    }

    private abstract static class Command {
        private String _s;

        public Command(String s) {
            _s = s;
        }

        @Override
        public String toString() {
            return _s;
        }

        public abstract Sequence execute(Sequence original, List tracks)
                throws Exception;
    }

    private abstract static class EventCommand extends Command {
        private long _tick;

        EventCommand(String s, long tick) {
            super(s);
            _tick = tick;
        }

        @Override
        public Sequence execute(Sequence original, List tracks)
                throws Exception {
            Track t;

            if (tracks.size() == 0) {
                t = original.createTrack();
                tracks.add(t);
            }
            else {
                t = (Track)tracks.get(tracks.size() - 1);
            }

            MidiMessage msg = createMessage();
            t.add(new MidiEvent(msg, _tick));

            return original;
        }

        @SuppressWarnings("unused")
        public long getTick() {
            return _tick;
        }

        public abstract MidiMessage createMessage() throws Exception;

    }

    private static class FmtCode extends Command {
        private float _divisionT;
        private int _division;

        FmtCode(String s) {
            super(s);

            String[] tmp = P_SPLIT.split(s, 4);

            _division = Integer.parseInt(tmp[3]);

            String c = tmp[2];
            if (CODE_PPQ.equals(c)) {
                _divisionT = Sequence.PPQ;
            }
            else if (CODE_SMPTE_24.equals(c)) {
                _divisionT = Sequence.SMPTE_24;
            }
            else if (CODE_SMPTE_25.equals(c)) {
                _divisionT = Sequence.SMPTE_25;
            }
            else if (CODE_SMPTE_30.equals(c)) {
                _divisionT = Sequence.SMPTE_30;
            }
            else if (CODE_SMPTE_30DROP.equals(c)) {
                _divisionT = Sequence.SMPTE_30DROP;
            }
            else {
                throw new IllegalArgumentException(s);
            }
        }

        @Override
        public Sequence execute(Sequence original, List tracks)
                throws Exception {
            tracks.clear();
            return new Sequence(_divisionT, _division);
        }
    }

    private static class TrackCode extends Command {
        TrackCode(String s) {
            super(s);
        }

        @Override
        public Sequence execute(Sequence original, List tracks)
                throws Exception {
            tracks.add(original.createTrack());
            return original;
        }
    }

    private static class SysexEventCommand extends EventCommand {
        String _sysexData;

        // fixme: special SysEx messages
        SysexEventCommand(String s, long tick) {
            super(s, tick);

            String[] tmp = P_SPLIT.split(s, 2);
            s = tmp[1];

            _sysexData = StringUtils
                    .fromJavaString(s.substring(1, s.length() - 1));
        }

        @Override
        public MidiMessage createMessage() throws Exception {
            SysexMessage msg = new SysexMessage();
            msg.setMessage(_sysexData.getBytes(), _sysexData.length());
            return msg;
        }
    }

    private static class MetaEventCommand extends EventCommand {
        int _metaType;
        String _metaData;

        MetaEventCommand(String s, long tick) {
            super(s, tick);

            String[] tmp = P_SPLIT.split(s, 3);
            s = tmp[2];
            _metaData = StringUtils
                    .fromJavaString(s.substring(1, s.length() - 1));
            // System.err.println("JavaString: " + s + " -> " +
            // new
            // org.xodonex.util.struct.PrimitiveArrayList(_metaData.getBytes()));
            _metaType = Integer.parseInt(tmp[1], 16);
        }

        @Override
        public MidiMessage createMessage() throws Exception {
            MetaMessage msg = new MetaMessage();

            msg.setMessage(_metaType, _metaData.getBytes(), _metaData.length());
            return msg;
        }
    }

    private static class ChannelEventCommand extends EventCommand {
        int _chn;
        int _status;
        int _data1;
        int _data2;

        ChannelEventCommand(String s, long tick) {
            super(s, tick);

            String[] tmp = P_SPLIT.split(s);

            _chn = Integer.parseInt(tmp[1], 16);
            _status = Integer.parseInt(tmp[2], 16);
            _data1 = Integer.parseInt(tmp[3], 16);
            _data2 = Integer.parseInt(tmp[4], 16);
        }

        @Override
        public MidiMessage createMessage() throws Exception {
            ShortMessage msg = new ShortMessage();

            msg.setMessage(_status, _chn, _data1, _data2);
            return msg;
        }
    }

}

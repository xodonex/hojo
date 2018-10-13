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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.SysexMessage;
import javax.sound.midi.Track;

/**
 *
 * @author Henrik Lauritzen
 */
public final class MidiUtils {

    public static Object observeSequence(Sequence s, MidiTrackObserver obs,
            Object log) {
        Track[] tracks = s.getTracks();

        for (int i = 0; i < tracks.length; i++) {
            log = obs.observe(tracks[i], log);
        }

        return log;
    }

    public static void filterSequence(Sequence s, MidiTrackFilter tf,
            int track) {
        Track[] tracks = s.getTracks();

        // iterate over the tracks
        for (int i = 0; i < tracks.length; i++) {
            if (track >= 0 && track != i) {
                // preserve the track unchanged
                continue;
            }

            // filter the track (filter needs the sequence in order to create a
            // new track, <sigh>. So it might just was well delete the tracks...
            tf.filter(s, tracks[i]);
        }
    }

    public static void filterSequence(Sequence s, MidiEventFilter flt,
            int track) {
        filterSequence(s, new TrackEventFilter(flt), track);
    }

    public static Track filterTrack(Sequence s, Track t, MidiEventFilter flt) {
        Track newTrack = s.createTrack();

        // filter the events of the original track, using the new one to store
        // events. One would expect to be able to modify tracks by using
        // add/remove, but the API scews that up.
        for (int i = 0; i < t.size(); i++) {
            MidiEvent e = t.get(i);

            if (flt.filter(e)) {
                newTrack.add(e);
            }
        }

        s.deleteTrack(t);
        return newTrack;
    }

    public static void main(String[] args) {
        try {
            Function f;

            Map m = new HashMap();
            m.put("decode", new DecodeFunction());
            m.put("encode", new EncodeFunction());
            m.put("filter", new FilterFunction());

            Function h = new HelpFunction();
            m.put("help", h);

            f = (Function)m.get(args.length < 1 ? "help" : args[0]);
            if (f == null) {
                f = h;
            }

            f.invoke(args, 1);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private MidiUtils() {
    }

    public static class TrackEventFilter implements MidiTrackFilter {
        MidiEventFilter _flt;

        public TrackEventFilter(MidiEventFilter flt) {
            _flt = flt;
        }

        @Override
        public void filter(Sequence s, Track t) {
            filterTrack(s, t, _flt);
        }
    }

    public static class BasicEventFilter implements MidiEventFilter {

        public boolean filterMeta(MidiEvent e, MetaMessage msg) {
            return true;
        }

        public boolean filterSysex(MidiEvent e, SysexMessage msg) {
            return true;
        }

        public boolean filterShort(MidiEvent e, ShortMessage msg) {
            return true;
        }

        @Override
        public final boolean filter(MidiEvent e) {
            MidiMessage m = e.getMessage();

            if (m instanceof MetaMessage) {
                return filterMeta(e, (MetaMessage)m);
            }
            else if (m instanceof SysexMessage) {
                return filterSysex(e, (SysexMessage)m);
            }
            else {
                return filterShort(e, (ShortMessage)m);
            }
        }
    }

    public static class NoDynamicsFilter extends BasicEventFilter {
        int _v;

        public NoDynamicsFilter(int noteOnVelocity) {
            _v = noteOnVelocity & 127;
        }

        @Override
        public boolean filterShort(MidiEvent e, ShortMessage msg) {
            int c = msg.getCommand();

            if (c == ShortMessage.NOTE_ON) {
                try {
                    msg.setMessage(c, msg.getChannel(), msg.getData1(), _v);
                }
                catch (Exception _e) {
                    _e.printStackTrace();
                }
            }
            else if (c == ShortMessage.CONTROL_CHANGE &&
                    msg.getData1() == MidiConstants.CTRL_CHANNEL_VOLUME) {
                return false;
            }

            return true;
        }
    }

    public static class SplitChannelFilter extends BasicEventFilter {
        private List[] _tracks;
        private int[] _playing;
        PrintWriter _fixme = new PrintWriter(System.out);
        List _fixme2;

        public SplitChannelFilter(int n) {
            _tracks = new List[n];
            _playing = new int[n];
            _fixme2 = new org.xodonex.util.struct.PrimitiveArrayList(_playing);

            for (int i = 0; i < n; i++) {
                _tracks[i] = new ArrayList();
                _playing[i] = -1;
            }
        }

        public void updateSequence(Sequence s) throws InvalidMidiDataException {
            for (int i = 0; i < _tracks.length; i++) {
                Track t = s.createTrack();

                for (Iterator it = _tracks[i].iterator(); it.hasNext();) {
                    t.add((MidiEvent)it.next());
                }
            }
        }

        @Override
        public boolean filterShort(MidiEvent e, ShortMessage msg) {
            try {
                int c = msg.getCommand();
                MidiCodec.decodeShort(e.getTick(), msg, _fixme);
                _fixme.flush();

                if (c == ShortMessage.NOTE_OFF) {
                    // find the channel that is currently playing the note
                    int note = msg.getData1();
                    for (int t = 0; t < _tracks.length; t++) {
                        if (_playing[t] == note) {
                            // found the track; change the channel and place the
                            // event in the appropriate track
                            ShortMessage newMsg = new ShortMessage();
                            newMsg.setMessage(msg.getCommand(), t,
                                    msg.getData1(), msg.getData2());
                            _tracks[t].add(new MidiEvent(newMsg, e.getTick()));
                            MidiCodec.decodeShort(e.getTick(), newMsg, _fixme);
                            _fixme.flush();

                            // update the internal state, and remove the event
                            // from the original track
                            _playing[t] = -1;
                            System.out.println(_fixme2);
                            return false;
                        }
                    }

                    // not currently playing the note - leave the message
                    // unchanged
                    System.out.println("note " + note + " OFF -> ???");
                    return true;
                }
                else if (c == ShortMessage.NOTE_ON) {
                    // find the first available channel
                    for (int t = 0; t < _tracks.length; t++) {
                        if (_playing[t] < 0) {
                            // found an available track; change the channel of
                            // the event and place it in the track
                            ShortMessage newMsg = new ShortMessage();
                            newMsg.setMessage(msg.getCommand(), t,
                                    msg.getData1(), msg.getData2());
                            _tracks[t].add(new MidiEvent(newMsg, e.getTick()));
                            MidiCodec.decodeShort(e.getTick(), newMsg, _fixme);
                            _fixme.flush();

                            // update the internal state, and remove the event
                            // from the original track
                            _playing[t] = msg.getData1();
                            System.out.println(_fixme2);
                            return false;
                        }
                    }

                    System.out.println("note " + msg.getData1() + " ON -> ???");
                    // not enough room for the note - leave the message
                    // unchanged
                    return true;
                }
                else {
                    // retain non-note events unchanged
                    return true;
                }
            }
            catch (InvalidMidiDataException ee) {
                // should not happen...
                ee.printStackTrace();
                return true;
            }
        }
    }

    private abstract static class Function {

        protected File getFile(String[] args, int offset) throws Exception {
            return new File(offset >= args.length ? "-" : args[offset]);
        }

        protected BufferedReader getReader(String[] args, int offset)
                throws Exception {
            File f = getFile(args, offset);
            if ("-".equals(f.getPath())) {
                return new BufferedReader(new InputStreamReader(System.in));
            }
            else {
                return new BufferedReader(new FileReader(f));
            }
        }

        protected BufferedWriter getWriter(String[] args, int offset)
                throws Exception {
            File f = getFile(args, offset);
            if ("-".equals(f.getPath())) {
                return new BufferedWriter(new OutputStreamWriter(System.out));
            }
            else {
                return new BufferedWriter(new FileWriter(f));
            }
        }

        protected int getInt(String[] args, int offset, int deflt) {
            if (offset >= args.length) {
                return deflt;
            }
            else {
                return Integer.parseInt(args[offset]);
            }
        }

        public abstract void invoke(String[] args, int offset) throws Exception;
    }

    private static class HelpFunction extends Function {
        @Override
        public void invoke(String[] args, int offset) throws Exception {
            System.out.println("MidiUtils <command> arg ...");
            System.out.println("  commands:");
            System.out.println("    decode <in> <out> : decode MIDI to ASCII");
            System.out.println("    encode <in> <out> : encode ASCII to MIDI");
            System.out.println(
                    "    filter <type> <in> <out> [params]: filter MIDI");
        }
    }

    private static class DecodeFunction extends Function {
        @Override
        public void invoke(String[] args, int offset) throws Exception {
            File fIn = getFile(args, offset);
            Writer w = getWriter(args, offset + 1);

            try {
                MidiCodec codec = new MidiCodec(w);
                codec.decodeSequence(fIn);
            }
            finally {
                w.close();
            }
        }
    }

    private static class EncodeFunction extends Function {
        @Override
        public void invoke(String[] args, int offset) throws Exception {
            BufferedReader r = getReader(args, offset);
            File o = getFile(args, offset + 1);

            try {
                MidiCodec codec = new MidiCodec();
                Sequence s = codec.encodeSequence(r);
                MidiSystem.write(s, codec.getPreferredFileType(), o);
            }
            finally {
                r.close();
            }
        }
    }

    private static class FilterFunction extends Function {
        @Override
        public void invoke(String[] args, int offset) throws Exception {
            File i = getFile(args, offset);
            File o = getFile(args, offset + 1);
            String type = args.length <= offset + 2 ? "dynamics"
                    : args[offset + 2];
            offset += 3;

            int fileFormat = MidiSystem.getMidiFileFormat(i).getType();
            Sequence s = MidiSystem.getSequence(i);

            if ("dynamics".equals(type)) {
                filterSequence(s,
                        new NoDynamicsFilter(getInt(args, offset, 80)),
                        getInt(args, offset + 1, -1));
            }
            else if ("split".equals(type)) {
                SplitChannelFilter flt = new SplitChannelFilter(
                        getInt(args, offset + 1, 1));
                filterSequence(s, flt, getInt(args, offset, -1));
                flt.updateSequence(s);
            }
            else {
                throw new IllegalArgumentException(
                        "Unknown filter type \"" + type + "\"");
            }

            MidiSystem.write(s, fileFormat, o);
        }
    }

}

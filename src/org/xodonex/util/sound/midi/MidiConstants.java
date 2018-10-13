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

/**
 *
 * @author Henrik Lauritzen
 */
public interface MidiConstants {

    public int META_SEQUENCE_NUMBER = 0x00;
    public int META_TEXT_EVENT = 0x01;
    public int META_COPYRIGHT_NOTICE = 0x02;
    public int META_TRACK_NAME = 0x03;
    public int META_INSTRUMENT_NAME = 0x04;
    public int META_LYRIC = 0x05;
    public int META_MARKER = 0x06;
    public int META_CUE_POINT = 0x07;
    public int META_CHANNEL_PREFIX = 0x20;
    public int META_END_OF_TRACK = 0x2F;
    public int META_SET_TEMPO = 0x51;
    public int META_SMTPE_OFFSET = 0x54;
    public int META_TIME_SIGNATURE = 0x58;
    public int META_KEY_SIGNATURE = 0x59;
    public int META_SEQUENCER_SPECIFIC = 0x7F;

    public int CTRL_CHANNEL_VOLUME = 0x07;

    public int DATA_NOTE_OFF = 0x80;
    public int DATA_NOTE_ON = 0x90;
    public int DATA_POLYPHONIC_AFTERTOUCH = 0xA0;
    public int DATA_CONTROL_CHANGE = 0xB0;
    public int DATA_PROGRAM_CHANGE = 0xC0;
    public int DATA_CHANNEL_AFTERTOUCH = 0xD0;
    public int DATA_PITCH_WHEEL = 0xE0;
    public int DATA_SYSTEM = 0xF0;

}

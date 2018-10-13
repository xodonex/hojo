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
package org.xodonex.hojo.lib;

import org.xodonex.util.StringUtils;

/**
 * @author Henrik Lauritzen
 */
public final class StdLib {

    public final AllFunction all = AllFunction.getInstance();
    public final AppFunction app = AppFunction.getInstance();
    public final AsyncFunction async = AsyncFunction.getInstance();
    public final CaptureFunction capture = CaptureFunction.getInstance();
    public final ChoiceFunction choose = ChoiceFunction.getInstance();
    public final ClassifyFunction classify = ClassifyFunction.getInstance();
    public final CollateFunction collate = CollateFunction.getInstance();
    public final CopyFunction copy = CopyFunction.getInstance();
    public final CountifFunction countif = CountifFunction.getInstance();
    public final CustomizeFunction customize = CustomizeFunction.getInstance();
    public final EditFunction edit = EditFunction.getInstance();
    public final FchooseFunction fchoose = FchooseFunction.getInstance();
    public final FilterFunction filter = FilterFunction.getInstance();
    public final FindFunction find = FindFunction.getInstance();
    public final FoldlFunction foldl = FoldlFunction.getInstance();
    public final FoldrFunction foldr = FoldrFunction.getInstance();
    public final FormatFunction format;
    public final GrepFunction grep = GrepFunction.getInstance();
    public final HelpFunction help = HelpFunction.getInstance();
    public final IterateFunction iterate = IterateFunction.getInstance();
    public final MapFunction map = MapFunction.getInstance();
    public final MkLibFunction mkLib = MkLibFunction.getInstance();
    public final MessageFunction msg = MessageFunction.getInstance();
    public final PartitionFunction partition = PartitionFunction.getInstance();
    public final PasswordFunction passwd = PasswordFunction.getInstance();
    public final PasteFunction paste = PasteFunction.getInstance();
    public final RevFunction rev = RevFunction.getInstance();
    public final SelectFunction select = SelectFunction.getInstance();
    public final ShowFunction show = ShowFunction.getInstance();
    public final SizeFunction sizeof = SizeFunction.getInstance();
    public final SortFunction sort = SortFunction.getInstance();
    public final SplitFunction split = SplitFunction.getInstance();
    public final SubstFunction subst = SubstFunction.getInstance();
    public final TransFunction trans = TransFunction.getInstance();

    public StdLib(StringUtils.Format fmt) {
        if (fmt == null) {
            throw new NullPointerException();
        }
        format = new FormatFunction(fmt);
    }

}

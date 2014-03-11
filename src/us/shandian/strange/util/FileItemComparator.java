package us.shandian.strange.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.text.Collator;

import us.shandian.strange.type.FileItem;

public class FileItemComparator implements Comparator<FileItem>
{
	
	public static void sortFileItems(final ArrayList<FileItem> items) {
		Collections.sort(items, new FileItemComparator());
	}
	
	@Override
	public int compare(FileItem p1, FileItem p2)
	{
		if (p1.isDir && !p2.isDir) {
			return -1;
		} else if(!p1.isDir && p2.isDir) {
			return 1;
		} else {
			return Collator.getInstance().compare(p1.name, p2.name);
		}
	}
	
}

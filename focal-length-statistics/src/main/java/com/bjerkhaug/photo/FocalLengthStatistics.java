package com.bjerkhaug.photo;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;

public class FocalLengthStatistics {

	private static final String IMAGE_DIRECTORY_PATH = "/Users/bjerkhaug/Pictures/iPhoto Library/Masters/";
	private static final Integer MAX_FOCAL_LENGTH = 250;
	private static final Integer MIN_FOCAL_LENGTH = 10;
	
	private static final List<String> INCLUDE_FILE_NAME_FILTER = Arrays.asList(
		".JPEG",
		".JPG"
	);
	
	private static final List<String> EXCLUDE_FILE_NAME_FILTER = Arrays.asList(
			"CIMG",
			"2004 - Beijing",
			"2002 - Barne-tv",
			"2002 - h-fest",
			"2003 - Dimmu",
			"2006-2008 - mobil",
			"2007 - Berlin"
	);
	
	public static void main(String[] args) throws Exception {
		File imageDirectory = new File(IMAGE_DIRECTORY_PATH);
		Map<Integer, Integer> focalLengthCount = createFocalLengthCount(imageDirectory);
		printFocalLengths(focalLengthCount);
	}

	private static Map<Integer, Integer> createFocalLengthCount(File imageDirectory) throws Exception {
		Map<Integer, Integer> focalLengthCount = new HashMap<Integer, Integer>();
		fillInFocalLengthCount(imageDirectory, focalLengthCount);
		return focalLengthCount;
	}

	private static void fillInFocalLengthCount(File imageDirectory, Map<Integer, Integer> focalLengthCount) throws Exception {
		for (File file : imageDirectory.listFiles()) {
			if (file.isDirectory()) {
				fillInFocalLengthCount(file, focalLengthCount);
			} else if (shouldBeIncluded(file)) {
				fillInFocalLength(getFocalLength(file), focalLengthCount);
			} else {
				//Ignore file
			}
		}
	}

	private static void fillInFocalLength(Integer focalLength,
			Map<Integer, Integer> focalLengthCount) {
		if (focalLength == null) return;
		if (focalLengthCount.containsKey(focalLength)) {
			focalLengthCount.put(focalLength, focalLengthCount.get(focalLength) + 1);
		} else {
			focalLengthCount.put(focalLength, 1);
		}
	}

	private static boolean shouldBeIncluded(File file) {
		for (String excludeParameter : EXCLUDE_FILE_NAME_FILTER) {
			if (file.getAbsolutePath().contains(excludeParameter)) {
				return false;
			}
		}
		for (String includeParameter : INCLUDE_FILE_NAME_FILTER) {
			if (file.getAbsolutePath().contains(includeParameter)) {
				return true;
			}
		}
		return false;
	}

	private static Integer getFocalLength(File jpegFile) throws Exception {
		Metadata metadata = ImageMetadataReader.readMetadata(jpegFile);
		ExifSubIFDDirectory directory = metadata.getDirectory(ExifSubIFDDirectory.class);
		if (directory == null) {
			System.err.println("Missing EXIF data for file " + jpegFile.getAbsolutePath());
			return null;
		}
		Integer focalLength = directory.getInteger(ExifSubIFDDirectory.TAG_FOCAL_LENGTH);
		return isFocalLengthValid(jpegFile.getAbsolutePath(), focalLength) ? focalLength : null;
	}

	private static boolean isFocalLengthValid(String jpegFileName, Integer focalLength) {
		if (focalLength == null) {
			System.err.println("File " + jpegFileName + " has no focal length.");
			return false;
		} else if (focalLength > MAX_FOCAL_LENGTH) {
			System.err.println("File " + jpegFileName + " has focal length greater than " 
					+ MAX_FOCAL_LENGTH + "mm: " + focalLength + "mm");
			return false;
		} else if (focalLength < MIN_FOCAL_LENGTH) {
			System.err.println("File " + jpegFileName + " has focal length less than " 
					+ MIN_FOCAL_LENGTH + "mm: " + focalLength + "mm");
			return false;
		} else {
			return true;
		}
	}

	private static void printFocalLengths(Map<Integer, Integer> focalLengthCount) {
		TreeSet<Integer> focalLengths = new TreeSet<Integer>(focalLengthCount.keySet());
		for (Integer focalLength : focalLengths) {
			System.out.println(focalLength + "mm: " + focalLengthCount.get(focalLength));
		}
		System.out.println("Total number of files: " + findTotalNumberOfFiles(focalLengthCount));
	}

	private static String findTotalNumberOfFiles(Map<Integer, Integer> focalLengthCount) {
		int sum = 0;
		for (Integer count : focalLengthCount.values()) {
			sum += count;
		}
		return Integer.toString(sum);
	}
	
}

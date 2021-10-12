package com.wesleycourtney.sinflex.drive_swapper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import it.sauronsoftware.cron4j.Scheduler;

public class App {

	private static String SHARE_NAME = "xyz";
	private static String BACKUP_FILE_NAME = "backup.txt";
	private static String PATH = "/";
	private static String PREVIOUS_STATE_FILE = "previousState.txt";

	public static void main(String[] args) {
		Scheduler s = new Scheduler();
		s.schedule("* * * * *", new Runnable() {
			public void run() {
				System.out.println("\n---------------------------\n");
				update();
				System.out.println("It ran.");
				System.out.println("\n---------------------------\n");
			}
		});
		s.start();
	}

	private static String trimVolumeName(String volumeName) {
		String volumeLetter = volumeName.substring(0, 1);
		return volumeLetter.toLowerCase();
	}

	private static boolean drivesChanged() {
		boolean ret = false;
		String driveList = getDriveList();
		String fileAsText = readFromFile(PREVIOUS_STATE_FILE);
		boolean changeHappened = !driveList.contentEquals(fileAsText);
		if (changeHappened) {
			ret = true;
		}
		return ret;
	}

	private static void writeToFile(String fileName, String str) {
		FileWriter myWriter;
		try {
			System.out.println("Writing to file: " + str);
			myWriter = new FileWriter(fileName);
			myWriter.write(str);
			myWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private static String getDriveList() {
		String driveString = "";
		File[] drives = File.listRoots();
		if (drives != null && drives.length > 0) {
			String delimiter = "";
			for (File aDrive : drives) {
				String fileName = aDrive + BACKUP_FILE_NAME;
				if (backupFileExists(fileName)) {
					String letter = trimVolumeName(aDrive.toString());
					driveString += delimiter + letter;
					delimiter = ",";
				}
			}
		}
		System.out.println("Getting drive list: " + driveString);
		return driveString;
	}

	private static boolean backupFileExists(String fileName) {
		File tempFile = new File(fileName);
		return tempFile.exists();
	}

	public static String readFromFile(String fileName) {
		String text = "";
		BufferedReader brTest;
		try {
			brTest = new BufferedReader(new FileReader(fileName));
			text = brTest.readLine();
			System.out.println("Reading from file: " + text);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return text;
	}

	private static void runCommand(String cmd) {
		System.out.println("Running Command...");
		Runtime rt = Runtime.getRuntime();
		try {
			rt.exec(cmd);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void createShare(String shareName, String path) {
		String cmd = "net share " + shareName + "=" + path;
		System.out.println("Creating Share...");
		runCommand(cmd);
	}

	private static void removeShare(String shareName) {
		String cmd = "net share " + shareName + " /delete /Y";
		System.out.println("Removed a share");
		runCommand(cmd);
	}

	private static void update() {
		if (drivesChanged()) {
			System.out.println("Drives have changed.");
			String drives = getDriveList();
			writeToFile(PREVIOUS_STATE_FILE, drives);
			removeShare(SHARE_NAME);
			String[] driveLetters = drives.split(",");
			String firstDriveLetter = driveLetters[0];
			createShare(SHARE_NAME, firstDriveLetter + ":" + PATH);
		} else {
			System.out.println("No Drive changes");
		}
	}

}

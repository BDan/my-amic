package org.amic;
/****************************************************************
 *  09.2009 Modified by BDan for the aMIC emulator project.
 *
 *  Original copyright notice:
 *  This is an intel hexfile parser written in java.
Copyright (C) 2002 Deva Seetharam http://www.dseetharam.org

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details. It is available at
http://www.gnu.org/licenses/gpl.html
 */


import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.FileNotFoundException;


/*
Intel Hex File Format. this description is taken from www.8052.com.
Pos Description
1   Record Marker: 
    The first character of the line is always a colon (ASCII 0x3A) 
    to identify the line as an Intel HEX file 

2-3 Record Length: 
    This field contains the number of data bytes in the register 
    represented as a 2-digit hexadecimal number. This is the 
    total number of data bytes, not including the checksum byte 
    nor the first 9 characters of the line.   

4-7 Address: 
    This field contains the address where the data should be 
    loaded into the chip. This is a value from 0 to 65,535 
    represented as a 4-digit hexadecimal value. 

8-9 Record Type: 
    This field indicates the type of record for this line. 
    The possible values are: 
    00=Register contains normal data. 
    01=End of File. 
    02=Extended address. 

10-? Data Bytes: 
    The following bytes are the actual data that 
    will be burned into the EPROM. The data is represented as 
    2-digit hexadecimal values. 

Last 2 Checksum: 
     The last two characters of the line are a checksum for the 
     line. The checksum value is calculated by taking the two's 
     complement of the sum of all the preceding data bytes, 
     excluding the checksum byte itself and the colon at the
     beginning of the line.
 */


public class HexFileParser {
	//***you have to specify the required MAX_CODE_LENGTH
	public static final int MAX_CODE_LENGTH=2048;
	public static final int RADIX=16;
	public static final int DATA_RECORD = 0;
	public static final int EOF_RECORD = 1;

	private static boolean DEBUG=false; //false;

	private BufferedReader br;

	static {
		if (System.getProperty("DEBUG") != null) DEBUG=true;
	}

	/**
	 * Constructs a CustomFileReader object.
	 *
	 * @param   file  HexFile
	 * @exception FileNotFoundException If the file can't be found
	 *                 
	 */

	public HexFileParser(File file) throws FileNotFoundException {
		if (file == null) throw new NullPointerException("File is null");
		br = new BufferedReader(new FileReader(file));
	}


	/**
	 * parses the hexfile and returns an array of bytes that actually contain 
	 * that would be burnt into the target device. the position of bytes
	 * in the array is determined by the specified address. If an element doesn't 
	 * have any code specified, it will contain a 0.
	 *
	 * @return returns an array of code bytes.
	 *                 
	 */

	public void parseFile(int []iData, int offset) throws IOException {
		String record = "";
		int dataLength = 0;
		int address = 0;
		//short maxAddress = 0;
		int recordType = 0;
		int[] data = null;
		int[] hexVals = null;
		int sum = 0;
		int computedChecksum = 0;
		int checksum = 0;
		int recordLength = 0;
		int i = 0;
		int j = 0;
		int k = 0;
		//int l = 0;
		int temp = 0;
		int recordNum = 0;
		//int[] code = new int[MAX_CODE_LENGTH];
		//int[] markers = new int[MAX_CODE_LENGTH];

		//Arrays.fill(markers, (int) 0);

		while ((record = br.readLine()) != null) {
			recordNum++;
			if (DEBUG) System.out.println("\n********************Record " + recordNum);

			recordLength = record.length();
			if (DEBUG) System.out.println("Record Len: " + recordLength);
			hexVals = new int[recordLength/2];

			dataLength =  (Integer.parseInt(record.substring(1,3), RADIX));
			if (DEBUG) System.out.println("Data Length: " + dataLength); 

			//allocate memory for data in this record.
			data = new int[dataLength];

			address =  (Integer.parseInt(record.substring(3, 7), RADIX));
			if (DEBUG) System.out.println("Address: " + address);

			recordType =  (Integer.parseInt(record.substring(7,9), RADIX));
			if (DEBUG) System.out.println("Record Type " + recordType);

			//Extract the code bytes.

			temp =  9 + (2 * dataLength);
			for (j = 9, k=0; j < temp; j = j + 2, k++) {
				int realAddr = address-offset+k; 
				if (realAddr > iData.length) 
					throw new IllegalArgumentException("Address outside of borders " +
							recordNum);
				data[k] =  (Integer.parseInt(record.substring(j, (j + 2)), RADIX));

				//code[l] = data[k];
				iData[realAddr]=data[k];
				//markers[l] =  0;

			}

			//Compute the sum of hex values
			k = 0;
			sum = 0;
			for (j = 1; j < (recordLength - 2); j = j + 2) {
				hexVals[k] =  (Integer.parseInt(record.substring(j, (j + 2)), RADIX));
				sum += hexVals[k];
				k++;
			}

			if (DEBUG) {
				System.out.println("Data: " );
				for (i=0; i < dataLength; i++) {
					System.out.print(data[i] + "\t");

					if (i != 0 && i % 5 == 0) 
						System.out.println();
				}
				if (i % 5 != 1) 
					System.out.println();
			}

			computedChecksum =   ((sum % 256)*-1)&0xFF; // * (0xFF));
			if (DEBUG) System.out.printf("Computed checksum: %02X\n", computedChecksum); 

			checksum =  (Integer.parseInt(record.substring((recordLength - 2), 
					recordLength), RADIX));
			if (DEBUG) System.out.printf("Checksum: %02X\n", checksum);

			if (computedChecksum != checksum) 
				throw new IllegalArgumentException("invalid checksum in record " + recordNum);
		}


	}

}



package menu;

import entity.*;

import java.io.IOException;
import java.io.RandomAccessFile;

import javax.swing.JOptionPane;

public class RandomFile
{
    public RandomAccessFile output;
    public RandomAccessFile input;

    public void createFile(String fileName)
    {
        RandomAccessFile file = null;

        try // open file for reading and writing
        {
            file = new RandomAccessFile(fileName, "rw");
        }
        catch (IOException ioException)
        {
            JOptionPane.showMessageDialog(null, "Error processing file!");
            System.exit(1);
        }

        finally
        {
            closeFile(file);
        }
    }

    public void openWriteFile(String fileName)
    {
        try
        {
            output = new RandomAccessFile(fileName, "rw");
        }
        catch (IOException ioException)
        {
            JOptionPane.showMessageDialog(null, "File does not exist!");
        }
    }

    public void closeFile(RandomAccessFile file)
    {
        try
        {
            if (file != null)
                file.close();
        }
        catch (IOException ioException)
        {
            JOptionPane.showMessageDialog(null, "Error closing file!");
            System.exit(1);
        }
    }

    public long addRecords(Employee employeeToAdd)
    {
        long currentRecordStart = 0;

        // object to be written to file
        RandomAccessEmployeeRecord record;

        try // output values to file
        {
            record = new RandomAccessEmployeeRecord(employeeToAdd.getEmployeeId(), employeeToAdd.getPps(),
                    employeeToAdd.getSurname(), employeeToAdd.getFirstName(), employeeToAdd.getGender(),
                    employeeToAdd.getDepartment(), employeeToAdd.getSalary(), employeeToAdd.getFullTime());

            output.seek(output.length());// Look for proper position
            record.write(output);// Write object to file
            currentRecordStart = output.length();
        }
        catch (IOException ioException)
        {
            JOptionPane.showMessageDialog(null, "Error writing to file!");
        }

        return currentRecordStart - RandomAccessEmployeeRecord.SIZE;
    }

    public void changeRecords(Employee newDetails, long byteToStart)
    {
        // object to be written to file
        RandomAccessEmployeeRecord record;
        try // output values to file
        {
            record = new RandomAccessEmployeeRecord(newDetails.getEmployeeId(), newDetails.getPps(),
                    newDetails.getSurname(), newDetails.getFirstName(), newDetails.getGender(),
                    newDetails.getDepartment(), newDetails.getSalary(), newDetails.getFullTime());

            output.seek(byteToStart);// Look for proper position
            record.write(output);// Write object to file
        }
        catch (IOException ioException)
        {
            JOptionPane.showMessageDialog(null, "Error writing to file!");
        }
    }

    public void deleteRecords(long byteToStart)
    {
        // object to be written to file
        RandomAccessEmployeeRecord record;

        try // output values to file
        {
            record = new RandomAccessEmployeeRecord();
            output.seek(byteToStart);// Look for proper position
            record.write(output);// Replace existing object with empty object
        }
        catch (IOException ioException)
        {
            JOptionPane.showMessageDialog(null, "Error writing to file!");
        }
    }

    public void openReadFile(String fileName)
    {
        try // open file
        {
            input = new RandomAccessFile(fileName, "r");
        }
        catch (IOException ioException)
        {
            JOptionPane.showMessageDialog(null, "File is not suported!");
        }
    }

    // Get position of first record in file
    public long getFirst()
    {
        long byteToStart = 0;

        try
        {
            // try to get file
            input.length();
        } // end try
        catch (IOException e) {e.printStackTrace();}

        return byteToStart;
    }

    // Get position of last record in file
    public long getLast()
    {
        long byteToStart = 0;

        try
        {
            // try to get position of last record
            byteToStart = input.length() - RandomAccessEmployeeRecord.SIZE;
        }// end try
        catch (IOException e) {e.printStackTrace();}

        return byteToStart;
    }

    // Get position of next record in file
    public long getNext(long readFrom)
    {
        long byteToStart = readFrom;

        try
        {
            // try to read from file
            input.seek(byteToStart);// Look for proper position in file
            // if next position is end of file go to start of file, else get next position
            if (byteToStart + RandomAccessEmployeeRecord.SIZE == input.length())
                byteToStart = 0;
            else
                byteToStart = byteToStart + RandomAccessEmployeeRecord.SIZE;
        }
        catch (Exception e) {e.printStackTrace();}
        return byteToStart;
    }

    // Get position of previous record in file
    public long getPrevious(long readFrom)
    {
        long byteToStart = readFrom;

        try
        {
            // try to read from file
            input.seek(byteToStart);// Look for proper position in file
            // if previous position is start of file go to end of file, else get previous position
            if (byteToStart == 0)
                byteToStart = input.length() - RandomAccessEmployeeRecord.SIZE;
            else
                byteToStart = byteToStart - RandomAccessEmployeeRecord.SIZE;
        } // end try
        catch (Exception e) {e.printStackTrace();}
        return byteToStart;
    }

    // Get object from file in specified position
    public Employee readRecords(long byteToStart)
    {
        Employee thisEmp;
        RandomAccessEmployeeRecord record = new RandomAccessEmployeeRecord();

        try
        {
            // try to read file and get record
            input.seek(byteToStart);// Look for proper position in file
            record.read(input);// Read record from file
        }
        catch (IOException e) {e.printStackTrace();}// end catch

        thisEmp = record;

        return thisEmp;
    }

    // Check if PPS Number already in use
    public boolean isPpsExist(String pps, long currentByteStart)
    {
        RandomAccessEmployeeRecord record = new RandomAccessEmployeeRecord();
        boolean ppsExist = false;
        long currentByte = 0;

        try
        {
            // try to read from file and look for PPS Number
            // Start from start of file and loop until PPS Number is found or search returned to start position
            while (currentByte != input.length() && !ppsExist)
            {
                //if PPS Number is in position of current object - skip comparison
                if (currentByte != currentByteStart)
                {
                    input.seek(currentByte);// Look for proper position in file
                    record.read(input);// Get record from file
                    // If PPS Number already exist in other record display message and stop search
                    if (record.getPps().trim().equalsIgnoreCase(pps))
                    {
                        ppsExist = true;
                        JOptionPane.showMessageDialog(null, "PPS number already exist!");
                    }
                }
                currentByte = currentByte + RandomAccessEmployeeRecord.SIZE;
            }
        }
        catch (IOException e) {e.printStackTrace();}

        return ppsExist;
    }

    // Check if any record contains valid ID - greater than 0
    public boolean isSomeoneToDisplay()
    {
        boolean someoneToDisplay = false;
        long currentByte = 0;
        RandomAccessEmployeeRecord record = new RandomAccessEmployeeRecord();

        try
        {
            // try to read from file and look for ID
            // Start from start of file and loop until valid ID is found or search returned to start position
            while (currentByte != input.length() && !someoneToDisplay)
            {
                input.seek(currentByte);// Look for proper position in file
                record.read(input);// Get record from file
                // If valid ID exist in stop search
                if (record.getEmployeeId() > 0)
                    someoneToDisplay = true;
                currentByte = currentByte + RandomAccessEmployeeRecord.SIZE;
            }
        }
        catch (IOException e) {e.printStackTrace();}

        return someoneToDisplay;
    }
}

///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS com.opencsv:opencsv:4.1

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

public class MergeFiles {

    public static void main(String[] args) throws IOException {
        try (FileReader testFR = new FileReader("test.csv");
                FileReader trainFR = new FileReader("train.csv");
                FileWriter mergeFR = new FileWriter("all.csv");
                CSVReader testCsvReader = new CSVReader(testFR);
                CSVReader trainCsvReader = new CSVReader(trainFR);
                CSVWriter mergeCsvWriter = new CSVWriter(mergeFR);) {

            String[] nextRecord;

            // we are going to read data line by line
            while ((nextRecord = testCsvReader.readNext()) != null) {
                mergeCsvWriter.writeNext(nextRecord);
            }

            trainCsvReader.readNext();// drop header
            while ((nextRecord = trainCsvReader.readNext()) != null) {
                mergeCsvWriter.writeNext(nextRecord);
            }
        }
    }
}

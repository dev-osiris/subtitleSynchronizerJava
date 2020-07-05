import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class subtitle {
    public static void main(String[] args) throws IOException {
        var reader = new Scanner(System.in);
        final String path = "C:\\Users\\Harish\\IdeaProjects\\basics2\\src\\ineptionTest.srt";
        final String filename = "C:\\Users\\Harish\\IdeaProjects\\basics2\\src\\ineptionTest.srt";
        boolean matchFound = false;

        System.out.println("enter no of seconds to be changed:\n" +
                            "(ex: 5 for adding five seconds\n" +
                            " -7 for subtracting seven seconds)");

        int numOfSecToBeChanged = reader.nextInt();
        List<String> timeList = new ArrayList<>();
        List<String> textList = new ArrayList<>();

        String data = Files.readString(Paths.get(filename));
        Matcher timeMatcher = timeRegex().matcher(data);
        Matcher textMatcher = textRegex().matcher(data);

        while (timeMatcher.find()) {
            matchFound = true;
            int subtitleSec, subtitleMin, subtitleHr, subtitleMilsec;
            subtitleHr = Integer.parseInt(timeMatcher.group(1));
            subtitleMin = Integer.parseInt(timeMatcher.group(3));
            subtitleSec = Integer.parseInt(timeMatcher.group(5));
            subtitleMilsec = Integer.parseInt(timeMatcher.group(7));
            if (numOfSecToBeChanged > 0)
                timeList.add(addTime(numOfSecToBeChanged, subtitleMilsec, subtitleSec, subtitleMin, subtitleHr));
            else {
                CheckDecreasingTime(filename, numOfSecToBeChanged);
                timeList.add(subtractTime(numOfSecToBeChanged, subtitleMilsec, subtitleSec, subtitleMin, subtitleHr));
            }
        }
        if (!matchFound) {
            System.out.println("ERROR: this subtitle file cannot be edited");
            System.exit(0);
        }
        while (textMatcher.find()){
            textList.add(textMatcher.group(3));
        }
        ArrayList<List<String>> arrayOfSegregatedLists;
        arrayOfSegregatedLists = seggregateList(timeList);
        List<String> leftList = arrayOfSegregatedLists.get(0);
        List<String> rightList = arrayOfSegregatedLists.get(1);

        writeTime(leftList, rightList, textList, filename);
    }

    static void writeTime(List<String> left, List<String> right, List<String> text, String filename) throws IOException {
        final String lineTerminator = System.lineSeparator() + System.lineSeparator();
        FileWriter myFile = new FileWriter(filename);   //truncates the file
        BufferedWriter writer = new BufferedWriter(myFile);
        var leftElement = left.iterator();
        var rightElement = right.iterator();
        var textElement = text.iterator();
        int count = 1;
        while (leftElement.hasNext() && rightElement.hasNext() && textElement.hasNext()){
            writer.write(count + "\n");
            writer.write(leftElement.next() + " --> " + rightElement.next());
            //writer.write(textElement.next() + "\r\n\r\n");
            writer.write(textElement.next() + lineTerminator);
            count++;
        }
        writer.close();
        System.out.println("done");
    }

    static void CheckDecreasingTime(String filename, int numOfSecToBeChanged) throws IOException {
        numOfSecToBeChanged = Math.abs(numOfSecToBeChanged);
        String data = Files.readString(Paths.get(filename));
        Pattern p = Pattern.compile("(\\d\\d)(:)(\\d\\d)(:)(\\d\\d)(,)(\\d\\d\\d)");
        Matcher textMatcher2 = p.matcher(data);
        int subtitleSec = 0, subtitleMin = 0, subtitleHr = 0;
        if (textMatcher2.find()) {
            subtitleHr = Integer.parseInt(textMatcher2.group(1));
            subtitleMin = Integer.parseInt(textMatcher2.group(3));
            subtitleSec = Integer.parseInt(textMatcher2.group(5));
        }
        if (numOfSecToBeChanged > (subtitleHr * 3600 + subtitleMin * 60 + subtitleSec)) {
            System.out.println("ERROR: too much decrease in time.");
            System.exit(0);
        }
    }

    static ArrayList<List<String>> seggregateList(List<String> list1){
        ArrayList<List<String>> arr1 = new ArrayList<>();
        int k = 0;
        List<String> segregateLeft = new ArrayList<>(),
                     segregateRight = new ArrayList<>();
        for(String item : list1){
            if (k % 2 == 0)
                segregateLeft.add(item);
            else
                segregateRight.add(item);
            k += 1;
        }
        arr1.add(segregateLeft);
        arr1.add(segregateRight);
        return arr1;
    }

    static Pattern timeRegex(){
        return Pattern.compile("(\\d\\d)(:)(\\d\\d)(:)(\\d\\d)(,)(\\d\\d\\d)");
    }

    static Pattern textRegex(){
        return Pattern.compile("(--> (\\d\\d:\\d\\d:\\d\\d,\\d\\d\\d)([\\S\\s]*?)\\r\\n\\r\\n)");
    }

    static String addTime(int numOfSec, int milsec, int sec, int min, int hr) {
        int min1, hr1;
        int sec1 = sec + numOfSec;
        if (sec1 > 60) {
            int x_min = sec1 / 60;
            sec1 = sec1 - x_min * 60;
            min1 = x_min + min;
            if (min1 > 60) {
                int a = min1 / 60;
                min1 = min1 - a * 60;
                hr1 = hr + a;
            } else if (min1 == 60) {
                min1 = 0;
                hr1 = hr + 1;
            } else {
                min1 = min + x_min;
                hr1 = hr;
            }
        } else if (sec1 == 60) {
            min1 = min + 1;
            if (min1 == 60) {
                min1 = 0;
                hr1 = hr + 1;
                sec1 = 0;
            } else {
                sec1 = 0;
                hr1 = hr;
            }
        } else {
            min1 = min;
            hr1 = hr;
        }
        return String.format("%02d", hr1) + ":" + String.format("%02d" ,min1) + ":" +
                String.format("%02d", sec1) + "," + String.format("%03d", milsec);
    }

    static String subtractTime(int numOfSec, int milsec, int sec, int min, int hr) {
        int sec1, min1, hr1;
        numOfSec = Math.abs(numOfSec);
        if (numOfSec > 3600) {
            hr1 = numOfSec / 3600;
            hr = hr - hr1;
            sec1 = numOfSec - hr1 * 3600;
            if (sec1 < 60) {
                if (sec >= sec1)
                    numOfSec = sec - sec1;
                else {
                    min = min - 1;
                    sec1 = sec1 - sec;
                    numOfSec = 60 - sec1;

                }
            } else if ((60 < sec1) && (sec1 < 3600)) {
                min1 = sec1 / 60;
                min = min - min1;
                sec1 = sec1 - min1 * 60;
                numOfSec = sec - sec1;
            } else if (sec1 == 60)
                min += 1;
        } else if ((60 < numOfSec) && (numOfSec < 3600)) {
            min1 = numOfSec / 60;
            min = min - min1;
            sec1 = numOfSec - min1 * 60;
            if (sec1 > sec){
                if (min > 0){
                    min--;
                }
                else {
                    min = 59;
                    hr--;
                }
                numOfSec = 60 - (sec1 - sec);
            }
            else
                numOfSec = sec - sec1;

        } else if ((0 < numOfSec) && (numOfSec < 60)) {
            if (numOfSec < sec)
                numOfSec = sec - numOfSec;
            else if (numOfSec > sec) {
                if (min == 0) {
                    hr -= 1;
                    min = 59;
                    numOfSec = 60 - (numOfSec - sec);
                }
                else {
                    min = min - 1;
                    sec1 = numOfSec - sec;
                    numOfSec = 60 - sec1;
                }
            }
            else        //when numOfSec = sec
                numOfSec = numOfSec - sec;

        } else if (numOfSec == 3600) {
            hr = hr - 1;
            numOfSec = sec;
        } else if (numOfSec == 60) {
            min -= 1;
            numOfSec = sec;
        } else if (numOfSec == 0)
            numOfSec = sec;
        return String.format("%02d", hr) + ":" + String.format("%02d" ,min) + ":" +
               String.format("%02d", numOfSec) + "," + String.format("%03d", milsec);
    }
}

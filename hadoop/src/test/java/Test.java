import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * Author: wttttt
 * Github: https://github.com/wttttt-wang/hadoop_inaction
 * Date: 2017-05-12
 * Time: 14:58
 */
public class Test {

    public static void main(String[] args){
        String line = "10.3.242.99 - - [07/Jun/2017:08:05:50 +0800] \"GET result.html?input=Can a non-alcoholic restaurant be a huge success?\\x22\" 400 173 \"-\" \"-\" \"-\"";
        Pattern r = Pattern.compile("\\?input=[^\\x22]*");
        Matcher m = r.matcher(line);
        if (m.find()){
            // ?Input=Mickey
            String input = line.substring(m.start(), m.end());

            System.out.println(input);

            String searchWords = input.split("=")[1].toLowerCase().replace("\\x22", "").replaceAll("[^a-z\\+\\s]", " ");

            System.out.println(searchWords);

            String[] words = searchWords.split("\\s+");

            for(int i = 0; i< words.length; i++) System.out.println(words[i]);
        }

        // System.out.print("ncakKDAND39i1mks,cjac".toLowerCase());
    }
}

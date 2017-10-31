import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import java.util.HashMap;
import java.util.Map;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.LinkedHashMap;

public class WordCount {

  public static class TokenizerMapper
       extends Mapper<Object, Text, Text, Text>{

    private final static IntWritable one = new IntWritable(1);
    private Text word = new Text();
    private Text file = new Text();

    public void map(Object key, Text value, Context context
                    ) throws IOException, InterruptedException {
      String fileName = ((FileSplit) context.getInputSplit()).getPath().getName();
      String simplified = value.toString();
      simplified = simplified.toLowerCase();
      simplified = simplified.replaceAll("[^a-z]"," ");
      StringTokenizer itr = new StringTokenizer(simplified);
      while (itr.hasMoreTokens()) {
        word.set(itr.nextToken());
	file.set(fileName);
        context.write(word, file);
      }
    }
  }

  public static class IntSumReducer
       extends Reducer<Text,Text,Text,Text> {
    private IntWritable result = new IntWritable();

    public void reduce(Text key, Iterable<Text> values,
                       Context context
                       ) throws IOException, InterruptedException {
      int sum = 0;
	Map<String,Integer> fileCounts = new HashMap<String,Integer>();

      for (Text val : values) {
	if(fileCounts.containsKey(val.toString())){
		fileCounts.put(val.toString(),fileCounts.get(val.toString())+1);
	}
	else{
		fileCounts.put(val.toString(),1);
	}
      }

	Map<String,Integer> sortedFileCounts = fileCounts.entrySet().stream()
		.sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
		.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
		(oldValue, newValue) -> oldValue, LinkedHashMap::new));

 	String resultString = "";

	for(String fileCount : sortedFileCounts.keySet())
	{
		resultString  += "\n< "+fileCount+", "+ sortedFileCounts.get(fileCount)+">";
	}

      context.write(key, new Text(resultString));
    }
  }

  public static void main(String[] args) throws Exception {
    Configuration conf = new Configuration();
    Job job = Job.getInstance(conf, "word count");
    job.setJarByClass(WordCount.class);
    job.setMapperClass(TokenizerMapper.class);
    //job.setCombinerClass(IntSumReducer.class);
    job.setReducerClass(IntSumReducer.class);
    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(Text.class);
    FileInputFormat.addInputPath(job, new Path(args[0]));
    FileOutputFormat.setOutputPath(job, new Path(args[1]));
    System.exit(job.waitForCompletion(true) ? 0 : 1);
  }
}

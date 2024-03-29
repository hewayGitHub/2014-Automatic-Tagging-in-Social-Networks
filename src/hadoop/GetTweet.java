package hadoop;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.filecache.DistributedCache;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

import preprocess.TweetTokenize;

public class GetTweet {
	public static class GetTweetMapper extends Mapper<Text, Text, Text, Text> {	
		static Map<String, String> name2ID = new HashMap<String, String>();
		static Set<String> wordSet = new HashSet<String>();
		
		@Override
		protected void setup(Context context) throws IOException, InterruptedException {
			try {
				Path[] cacheFiles = DistributedCache.getLocalCacheFiles(context.getConfiguration());
				
				if (cacheFiles != null && cacheFiles.length > 1) {
					BufferedReader inName2ID = new BufferedReader(new FileReader(cacheFiles[0].toString()));
					
					try {
						String line, items[];
						while (null != (line = inName2ID.readLine())) {
							items = line.split("\t");
							
							if(items.length >= 2) {
								name2ID.put(items[0], items[1]);
							}
						}
					} finally {
						inName2ID.close();
					}
					
					BufferedReader inWord = new BufferedReader(new FileReader(cacheFiles[1].toString()));
					
					try {
						String line, items[];
						while (null != (line = inWord.readLine())) {
							items = line.split("\t");
							
							if(items.length >= 2) {
								wordSet.add(items[0]);
							}
						}
					} finally {
						inWord.close();
					}
				} else {
					System.err.println("cannot read from DistributedCache. cacheFiles:" + (cacheFiles == null?"null":cacheFiles.length));
					throw new RuntimeException();
				}
				
				System.out.println("Num of name2ID:" + name2ID.size());
			} catch (IOException e) {
				System.err.println("Exception reading DistributedCache: " + e);
			}
		}
		
		@Override
		protected void map(Text key, Text value, 
	            Context context) throws IOException, InterruptedException {
			String name = key.toString().trim();//key为昵称 value为tweet
			
			if(name2ID.containsKey(name)) {
				String tweet = value.toString();
				List<String> toks = TweetTokenize.tokenizeRawTweetText(tweet);
				
				if(toks.size() > 0) {
					boolean isEmpty = true;
					StringBuilder sb = new StringBuilder();
					for(int i = 0; i < toks.size() - 1; i++) {
						if(wordSet.contains(toks.get(i))) {
							sb.append(toks.get(i) + " ");
							isEmpty = false;
						}
					}
					
					if(wordSet.contains(toks.get(toks.size() - 1))) {
						sb.append(toks.get(toks.size() - 1));
						isEmpty = false;
					}
					
					if(!isEmpty) {
						value.set(sb.toString());
						context.write(new Text(name2ID.get(name)), value);//ID和tweet
					}
				}
			}
			
			/*if(name2ID.containsKey(name)) {
				context.write(new Text(name2ID.get(name)), value);//ID和tweet
			}*/
			
		}
	}
	
	public static class GetTweetReducer extends Reducer<Text, Text, Text, Text> {		
		@Override
		protected void reduce(Text key, Iterable<Text> values, Context context
	            ) throws IOException, InterruptedException {
			Iterator<Text> it = values.iterator();
			StringBuilder sb = new StringBuilder(it.next().toString());
			while(it.hasNext()) {
				sb.append("\t" + it.next().toString());
			}
			
			context.write(key, new Text(sb.toString()));//ID和tweet
		}
	}
	
	/**
	 * @param args
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 * @throws InterruptedException 
	 * @throws URISyntaxException 
	 */
	public static void main(String[] args) throws IOException, InterruptedException, ClassNotFoundException, URISyntaxException {
		Configuration jobConf = new Configuration();
		jobConf.setBoolean("mapreduce.map.output.compress", true);
		jobConf.setClass("mapreduce.map.output.compress.codec", org.apache.hadoop.io.compress.GzipCodec.class, org.apache.hadoop.io.compress.CompressionCodec.class);
		DistributedCache.addCacheFile(new Path("hdfs://t1:9000/heway/target_normal_user_info").toUri(), jobConf); //添加分布式缓存文件
		DistributedCache.addCacheFile(new Path("hdfs://t1:9000/heway/word_1000").toUri(), jobConf); //添加分布式缓存文件
		
		Job job = new Job(jobConf);
		job.setJobName("GetTweet");
		job.setJarByClass(GetTweet.class);
		
		job.setMapperClass(GetTweetMapper.class);
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(Text.class);
		
		job.setCombinerClass(GetTweetReducer.class);
		
		job.setReducerClass(GetTweetReducer.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
		
		job.setInputFormatClass(TweetInputFormat.class);
		job.setOutputFormatClass(TextOutputFormat.class);
		
		FileInputFormat.setInputPaths(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));
		
		//设置压缩
		FileOutputFormat.setCompressOutput(job, true);
		FileOutputFormat.setOutputCompressorClass(job, org.apache.hadoop.io.compress.GzipCodec.class);
		
		job.waitForCompletion(true);
	}

}

package standalone;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import preprocess.TweetTokenize;

public class AnaCelebrity {
	public static void extractFeatures() throws IOException {
		PrintWriter outCelebrityFeatures = new PrintWriter("D:\\twitter\\Twitter network\\celebrities_features");
		
		BufferedReader inCelebrity = new BufferedReader(new FileReader("D:\\twitter\\Twitter network\\celebrities_profiles.txt"));
		String line, items[], uid, description, screenName, name;
		while(null != (line=inCelebrity.readLine())) {
			items = line.split("\t");
			
			if(items.length < 20) continue;
			
			uid = items[0];
			description = items[10];
			screenName = items[16];
			name = items[19];
			
			outCelebrityFeatures.println(uid + "\t" + name + "\t" + screenName + "\t" + description);
		}
		inCelebrity.close();
		outCelebrityFeatures.close();
	}
	
	public static void handleFeatures() throws IOException {
		PrintWriter outCelebrityFeatures = new PrintWriter("D:\\twitter\\Twitter network\\celebritis_tokens");
		
		BufferedReader inCelebrity = new BufferedReader(new FileReader("D:\\twitter\\Twitter network\\celebrities_features"));
		String line, items[], uid, description, name;
		while(null != (line=inCelebrity.readLine())) {
			items = line.split("\t");
			
			if(items.length < 4) continue;
			
			uid = items[0];
			name = items[1];
			description = items[3];
			
			List<String> features = TweetTokenize.tokenizeRawTweetText(name + " " + description);
			if(features.size() != 0) {
				outCelebrityFeatures.print(uid + "\t");
				for(int i = 0; i< features.size(); i++) {
					if(i != features.size()-1)
						outCelebrityFeatures.print(features.get(i) + " ");
					else
						outCelebrityFeatures.print(features.get(i));
				}
				outCelebrityFeatures.println();
			}
		}
		inCelebrity.close();
		
		outCelebrityFeatures.close();
	}
	
	private static Set<String> getCelebrityIDs() throws IOException{
		Set<String> idSet = new HashSet<String>();
		
		BufferedReader inCelebrity = new BufferedReader(new FileReader("D:\\twitter\\Twitter network\\celebrities_features"));
		String line, items[], uid;
		while(null != (line=inCelebrity.readLine())) {
			items = line.split("\t");
			
			if(items.length < 4) continue;
			
			uid = items[0];
			idSet.add(uid);
		}
		inCelebrity.close();
		
		return idSet;
	}
	
	public static void countCelebrities() throws IOException {
		PrintWriter outNormalCelebrityCount = new PrintWriter("D:\\twitter\\Twitter network\\normal_user_celebrities_count");
		
		Set<String> idSet = getCelebrityIDs();
		
		BufferedReader inCelebrity = new BufferedReader(new FileReader("D:\\twitter\\Twitter network\\normal_user_follows"));
		String line, items[], uid, follows[];
		int count = 0;
		while(null != (line=inCelebrity.readLine())) {
			items = line.split("\t");
			
			if(items.length < 2) continue;
			
			uid = items[0];
			follows = items[1].split(" ");
			
			int followCount = 0;
			for(String follow: follows) {
				if(idSet.contains(follow)) {					
					followCount++;
				}
			}
			if(followCount != 0) {				
				outNormalCelebrityCount.println(uid + "\t" + followCount);
				count++;
			}
		}
		inCelebrity.close();
		outNormalCelebrityCount.close();
		
		System.out.println(count);
	}
	
	static Map<String, String> getIDMap(String userPath, int userNum) throws IOException {	
		Map<String, String> nameMap = new HashMap<String, String>(userNum);
		
		BufferedReader inUser = new BufferedReader(new FileReader(userPath));

		String line = null, uid;
		int firstTapIndex = -1, secondTapIndex;
		while (null != (line = inUser.readLine())) {
			firstTapIndex = line.indexOf('\t');
			if (firstTapIndex != -1) {
				secondTapIndex = line.indexOf('\t', firstTapIndex + 1);
				uid = line.substring(firstTapIndex + 1, secondTapIndex);
				nameMap.put(uid, line);
			}
		}
		
		inUser.close();
		
		return nameMap;
	}
	
	private static Set<String> getTargetIDs() throws IOException{
		Set<String> idSet = new HashSet<String>();
		
		BufferedReader inCelebrity = new BufferedReader(new FileReader("D:\\twitter\\Twitter network\\normal_user_celebrities_count"));
		String line, items[], uid;
		int celebrityCount;
		while(null != (line=inCelebrity.readLine())) {
			items = line.split("\t");
			
			if(items.length < 2) continue;
			
			uid = items[0];
			celebrityCount = Integer.parseInt(items[1]);
			
			if(celebrityCount >= 50) {
				idSet.add(uid);
			}
		}
		inCelebrity.close();
		
		return idSet;
	}
	
	private static Map<String, String> getTargetIDMap() throws IOException{
		Map<String, String> idSet = new HashMap<String, String>();
		
		BufferedReader inCelebrity = new BufferedReader(new FileReader("D:\\twitter\\Twitter network\\normal_user_celebrities_count"));
		String line, items[], uid;
		int celebrityCount;
		while(null != (line=inCelebrity.readLine())) {
			items = line.split("\t");
			
			if(items.length < 2) continue;
			
			uid = items[0];
			celebrityCount = Integer.parseInt(items[1]);
			
			if(celebrityCount >= 50) {
				idSet.put(uid, ""+celebrityCount);
			}
		}
		inCelebrity.close();
		
		return idSet;
	}
	
	public static void filterFollows() throws IOException {
		PrintWriter outNormalCelebrity = new PrintWriter("D:\\twitter\\Twitter network\\normal_user_celebrities");
		
		Set<String> targetIdSet = getTargetIDs();
		Set<String> celebrityIdSet = getCelebrityIDs();
		System.out.println("target user:" + targetIdSet.size() + " celebrity: " + celebrityIdSet.size());
		
		BufferedReader inCelebrity = new BufferedReader(new FileReader("D:\\twitter\\Twitter network\\normal_user_follows"));
		String line, items[], uid, follows[];
		int count = 0;
		while(null != (line=inCelebrity.readLine())) {
			items = line.split("\t");
			
			if(items.length < 2) continue;
			
			uid = items[0];
			
			if(targetIdSet.contains(uid)) {
				count++;
				
				follows = items[1].split(" ");
				
				StringBuilder sb = new StringBuilder();
				for(String follow: follows) {
					if(celebrityIdSet.contains(follow)) {					
						sb.append(follow + " ");
					}
				}
				sb.deleteCharAt(sb.length() - 1);
			
				outNormalCelebrity.println(uid + "\t" + sb.toString());
			}
		}
		inCelebrity.close();
		outNormalCelebrity.close();
		
		System.out.println(count);
	}
	
	public static void filterTweets() throws IOException {
		PrintWriter outNormalCelebrity = new PrintWriter("D:\\twitter\\Twitter network\\normal_user_tweets_filter");
		
		Set<String> idSet = getTargetIDs();
		System.out.println(idSet.size());
		
		BufferedReader inCelebrity = new BufferedReader(new FileReader("D:\\twitter\\Twitter network\\normal_user_tweets"));
		String line, items[], uid;
		int count = 0;
		while(null != (line=inCelebrity.readLine())) {
			items = line.split("\t");
			
			if(items.length < 2) continue;
			
			uid = items[0];
			
			if(idSet.contains(uid)) {
				outNormalCelebrity.println(line);
				count++;
			}
		}
		inCelebrity.close();
		outNormalCelebrity.close();
		
		System.out.println(count);
	}
	
	public static void anaFollows() throws IOException {
		Set<String> celebrityIdSet = new HashSet<String>();
		BufferedReader inCelebrity = new BufferedReader(new FileReader("D:\\twitter\\Twitter network\\normal_user_celebrities"));
		String line, items[], follows[];
		while(null != (line=inCelebrity.readLine())) {
			items = line.split("\t");
			
			if(items.length < 2) continue;
			
			follows = items[1].split(" ");
				
			for(String follow: follows) {
				celebrityIdSet.add(follow);
			}
		}
		inCelebrity.close();
		
		PrintWriter outNormalCelebrity = new PrintWriter("D:\\twitter\\Twitter network\\celebrities_uid");
		for(String uid: celebrityIdSet) {
			outNormalCelebrity.println(uid);
		}
		outNormalCelebrity.close();
	}
	
	private static Map<String, String> getCelebrityDescription() throws IOException{
		Map<String, String> idMap = new HashMap<String, String>();
		
		BufferedReader inCelebrity = new BufferedReader(new FileReader("D:\\twitter\\Twitter network\\celebritis_tokens"));
		String line, items[], uid, tokens;
		while(null != (line=inCelebrity.readLine())) {
			items = line.split("\t");
			
			if(items.length < 2) continue;
			
			uid = items[0];
			tokens = items[1];
			idMap.put(uid, tokens);
		}
		inCelebrity.close();
		
		return idMap;
	}
	
	public static void getFollowsDescription() throws IOException {
		PrintWriter outNormalCelebrity = new PrintWriter("D:\\twitter\\Twitter network\\normal_user_celebrities_tokens");
		
		Map<String, String> celebrityDescr = getCelebrityDescription();
		
		BufferedReader inCelebrity = new BufferedReader(new FileReader("D:\\twitter\\Twitter network\\normal_user_celebrities"));
		String line, items[], uid, follows[];
		while(null != (line=inCelebrity.readLine())) {
			items = line.split("\t");
			
			if(items.length < 2) continue;
			
			uid = items[0];
			follows = items[1].split(" ");
				
			outNormalCelebrity.print(uid + "\t");
			StringBuilder sb = new StringBuilder();
			for(int i = 0; i < follows.length; i++) {
				String follow = follows[i];
				
				if(i == follows.length - 1)
					sb.append(celebrityDescr.get(follow));
				else
					sb.append(celebrityDescr.get(follow) + " ");
			}
			outNormalCelebrity.println(sb.toString());
		}
		inCelebrity.close();
		outNormalCelebrity.close();
	}
	
	public static void main(String[] args) throws IOException {
		//extractFeatures();
		//countCelebrities();
		//filterFollows();
		//filterTweets();
		//anaFollows();
		
		//handleFeatures();
		//getFollowsDescription();
		
		Map<String, String> normalIDMap = getIDMap("D:\\twitter\\Twitter network\\normal_user_info", 146685);
		PrintWriter outTargetID = new PrintWriter("D:\\twitter\\Twitter network\\target_normal_user_info");
		Map<String, String> targetIDs = getTargetIDMap();
		for(String id: targetIDs.keySet()) {
			outTargetID.println(normalIDMap.get(id) + "\t" + targetIDs.get(id));
		}
		outTargetID.close();
	}

}

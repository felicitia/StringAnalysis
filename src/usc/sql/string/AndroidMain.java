package usc.sql.string;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import soot.Scene;
import soot.options.Options;
import usc.yixue.ViolistAnalysisHelper;

public class AndroidMain {

	public static void main(String[] args) {
		
		//JavaAndroid ja = new JavaAndroid(args[0],args[1],args[2],args[3],args[4],Integer.parseInt(args[5]),Integer.parseInt(args[6]));
	
		List<Integer> paraSet = new ArrayList<>();
		paraSet.add(1);
		Map<String,List<Integer>> target = new HashMap<>();
		target.put("<java.net.URL: void <init>(java.lang.String)>",paraSet);

/**
 * args:
 * args[0]: /Users/felicitia/Documents/Research/Prefetch/Develop/Yingjun/Android
	args[1]: /Users/felicitia/Documents/Research/Prefetch/Develop/Yingjun/Android/WeatherApp
	args[2]: /classlist.txt 
	args[3]: /app-release(no_xml_handler).apk
 */
		JavaAndroid ja = new JavaAndroid(args[0],args[1],args[2],args[3],target,1);
		
//		AnalysisHelper.outputRequestMap(AnalysisHelper.getTargetStmtList(args[1]), args[1]+"/"+args[4]+".json");
	}
}

package exam;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class EaxmCore {

	public static final String SCORERULE_PER = "per";
	private Properties props;
	private boolean showLog=true;
	
	public void show(String info){
		if(showLog){
			System.out.println(info);
		}
	}

	// 阅卷:改一套试卷
	public void yueJuan() {
		// 读取参考答案
		Map<Integer, String> map = huoDeCanKao();
		show("结果：" + map);
		// 读取所有学生答题
		Map<String, Map<Integer, String>> stuAnswer = readStusAnswers();

		// 判断是否正确并计分
		Map<String, Double> scores = scoring(map, stuAnswer);
		show("分数：" + scores);
		
		// 写出成绩到文件
		writeResults(scores);

	}

	// 判断是否正确并计分
	/**
	 * 判断答案是否正确并计分
	 * @param refAnswers  参考答案
	 * @param stuAnswers  考生答案
	 * @return	分数
	 */
	private Map<String, Double> scoring(Map<Integer, String> refAnswers,
			Map<String, Map<Integer, String>> stuAnswers) {
		// 计分结果
		Map<String, Double> scores = new HashMap<String, Double>();
		// 计分
		Set<String> stuNames = stuAnswers.keySet();
		for (String stuName : stuNames) {
			Map<Integer, String> answers = stuAnswers.get(stuName);
			double score = judgeAnswers(refAnswers, answers);
			scores.put(stuName, score);
		}
		return scores;

	}

	// 计算一套题的总分
	/**
	 * 
	 * @param map
	 * @param answers
	 * @return
	 */
	private double judgeAnswers(Map<Integer, String> map,
			Map<Integer, String> answers) {
		double totalScore = 0;
		Set<Integer> questionNums = answers.keySet();
		// 判断每个题是否正确并计分
		for (Integer questionNum : questionNums) {
			String stuAnswer = answers.get(questionNum);
			String canKao = map.get(questionNum);
			System.out.println(questionNum);
			// 判断是否正确
			boolean isRight = checkRight(canKao, stuAnswer);
			if (isRight) {
				// 根据计分规则算分
				totalScore += getScore(questionNum);
			}

		}
		return totalScore;
	}

	// 根据题号获取分数
	private double getScore(Integer questionNum) {
		double perScore = 2;
		String scoreRule = props.getProperty("scoreRule");
		if (SCORERULE_PER.equals(scoreRule)) {
			if (questionNum <= 20) {
				perScore = 1.5;
			} else if (questionNum <= 40) {
				perScore = 2;
			} else {
				perScore = 3;
			}
		}
		return perScore;
	}

	// 判断两个字符串是否相等（不分前后，不分大小）
	private boolean checkRight(String canKao, String stuAnswer) {
		System.out.println(canKao+"\t"+stuAnswer);
		//判断是否为空
		if (canKao ==null || stuAnswer ==null) {
			return false;
		}
		// 如果长度一样
		if (canKao.length() == stuAnswer.length()) {
			// 转换为字符数组并排序，然后比较是否相等
			char[] charArray = canKao.toUpperCase().toCharArray();
			Arrays.sort(charArray);
			canKao = new String(charArray);
			charArray = stuAnswer.toUpperCase().toCharArray();
			Arrays.sort(charArray);
			stuAnswer = new String(charArray);
			return canKao.equals(stuAnswer);

		} else {
			return false;
		}
	}

	// 写出成绩
	private void writeResults(Map<String, Double> scores) {
		File file = new File("score.txt");
		PrintWriter out = null;
		try {
			out = new PrintWriter(file);
			Set<String> stuNames = scores.keySet();
			int i=1;
			for (String stuName : stuNames) {
				out.println(i+"\t"+stuName + "\t" + scores.get(stuName));
				i++;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			if (out != null) {
				out.close();
			}
		}

	}

	// 获取所有学生的答题
	private Map<String, Map<Integer, String>> readStusAnswers() {
		// 创建合集
		Map<String, Map<Integer, String>> map = new HashMap<String, Map<Integer, String>>();
		// 获取指定目录下的所有文件
		String path = props.getProperty("stuAnswerPath");
		File file = new File(path);
		final String suffix = props.getProperty("suffix");
		String[] files = file.list(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {

				return name.endsWith(suffix);
			}
		});

		String stuName;
		// 读取每个学生的答案并保存到map中
		for (String fileName : files) {
			stuName = fileName.substring(0, fileName.lastIndexOf("."));
			Map<Integer, String> stuAnswer = readAnswer(path + fileName);
			map.put(stuName, stuAnswer);
		}
		// System.out.println(map);
		return map;
	}

	// 读取参考答案
	private Map<Integer, String> huoDeCanKao() {
		// 参考答案文件名
		String fileName = props.getProperty("ckFileName");
		return readAnswer(fileName);
	}

	// 读取答案
	private Map<Integer, String> readAnswer(String fileName) {
		
		System.out.println(fileName);
		// 答案集合
		Map<Integer, String> map = new HashMap<Integer, String>();
		// 题号和答案之间的分隔符
		String regex = props.getProperty("regex");
		FileInputStream in = null;
		try {
			in = new FileInputStream(fileName);
			InputStreamReader sreader=new InputStreamReader(in, "gbk");
			BufferedReader reader=new BufferedReader(sreader);
			String[] strs;
			String line = "";
			while ((line = reader.readLine()) != null) {
				line=new String(line.getBytes("gbk"),"gbk");
				
				strs = line.replaceAll("\\s", "").split(regex);
				//System.out.println(Arrays.toString(strs));
				if (strs != null && strs.length == 2) {
					map.put(Integer.parseInt(strs[0].trim()), strs[1]);
				}

			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return map;
	}

	public static void main(String[] args) {
		EaxmCore core = new EaxmCore();
		core.loadProps("src/exam.properties");
		core.yueJuan();
		// core.readStusAnswers();
	}

	// 加载指定的考试配置文件
	public void loadProps(String path) {
		File file = new File(path);
		if (file.exists()) {
			props = new Properties();
			FileReader fileReader = null;
			try {
				fileReader = new FileReader(file);
				props.load(fileReader);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					if (fileReader != null)
						fileReader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} else {
			System.out.println(file.getAbsolutePath() + "找不到");
		}
		System.out.println(props);
	}
}

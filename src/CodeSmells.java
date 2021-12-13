import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CodeSmells {

	private static int godlines = 0, badidentifierclasses = 0, longparamclasses = 0, largeclasses = 0,
			freeloaderclasses = 0, cyclocomplex = 0, badidentifiermethods = 0, longparammethods = 0, longmethods = 0,
			freeloadermethods = 0;

	public static void main(String[] args) throws Exception {
		BufferedWriter writer = new BufferedWriter(new FileWriter("code smells report.txt"));
		String smells;
		List<File> files = getPythonFiles();
		for (int i = 0; i < files.size(); i++) {
			writer.write(files.get(i).getPath() + "\n");
			System.out.println(files.get(i).getPath());
			Map<Integer, String> lines = readPython(files.get(i));
			writer.write(findGodLines(lines));
			List<int[]> classBlocks = getClassBlocks(lines);
			smells = "";
			for (int b = 0; b < classBlocks.size(); b++) {
				 smells += findClassSmells(lines, classBlocks.get(b)[0], classBlocks.get(b)[1]);
			}
			if (!smells.isBlank()) {
				writer.write("Classes:\n" + smells);
			}
			List<int[]> methodBlocks = getMethodBlocks(lines);
			smells = "";
			for (int b = 0; b < methodBlocks.size(); b++) {
				smells += findMethodSmells(lines, methodBlocks.get(b)[0], methodBlocks.get(b)[1]);
			}
			if (!smells.isBlank()) {
				writer.write("Methods:\n" + smells);
			}
			writer.write("\n");
			writer.flush();
		}
		writer.write(countTotals());
		writer.flush();
		writer.close();
	}

	private static List<File> getPythonFiles() throws Exception {
		List<File> r = new ArrayList<>();
		Stream<Path> walk = Files.walk(Path.of("raw"));
		List<Path> paths = walk.filter(Files::isRegularFile).collect(Collectors.toList());
		walk.close();
		for (int i = 0; i < paths.size(); i++) {
			if (paths.get(i).toString().endsWith(".py")) {
				r.add(paths.get(i).toFile());
			}
		}
		return r;
	}

	private static Map<Integer, String> readPython(File f) throws Exception {
		Map<Integer, String> r = new HashMap<>();
		BufferedReader reader = new BufferedReader(new FileReader(f));
		String line;
		for (int i = 1; (line = reader.readLine()) != null; i++) {
			line = line.replaceAll("//s+$", "");
			r.put(i, line);
			System.out.println(i + line);
		}
		reader.close();
		return r;
	}

	private static String findGodLines(Map<Integer, String> lines) {
		String r = "";
		for (int l = 1; l <= lines.size(); l++) {
			if (lines.get(l).length() > 200) {
				godlines++;
				if (r.isBlank()) {
					r = "God lines: " + l;
				} else {
					r += ", " + l;
				}
			}
		}
		return r.isBlank() ? r : r + "\n\n";
	}

	private static List<int[]> getClassBlocks(Map<Integer, String> lines) {
		List<int[]> r = new ArrayList<>();
		for (int i = 1; i <= lines.size(); i++) {
			String line = lines.get(i);
			if (line.trim().length() > 0 && line.indexOf(line.trim()) < 4) {
				if (!r.isEmpty()) {
					int[] temp = r.get(r.size() - 1);
					int l = i - 1;
					while (lines.get(l).isBlank()) {
						l--;
					}
					temp[1] = l;
					r.set(r.size() - 1, temp);
					System.out.println(Arrays.toString(temp));
				}
			}
			if (line.startsWith("class ")) {
				r.add(new int[] { i, 0 });
			}
			if (i == lines.size() && !r.isEmpty() && r.get(r.size() - 1)[1] == 0) {
				int[] temp = r.get(r.size() - 1);
				int l = i;
				while (lines.get(l).isBlank()) {
					l--;
				}
				temp[1] = l;
				r.set(r.size() - 1, temp);
				System.out.println(Arrays.toString(temp));
			}
		}
		return r;
	}

	public static String findClassSmells(Map<Integer, String> lines, int start, int end) {
		String r = "\t" + start + " ", line = lines.get(start++);
		while (!line.endsWith(":")) {
			line += lines.get(start++).trim();
		}
		String name = line.split(" |\\(|:")[1];
		System.out.println(name);
		r += name;
		if (name.length() < 3 || name.length() > 20) {
			badidentifierclasses++;
			r += "\n\t\tBad identifier";
		}
		String[] parameters = line.indexOf("(") > -1
				? line.substring(line.indexOf("(") + 1, line.indexOf(")")).split(", *")
				: new String[0];
		System.out.println(parameters.length);
		if (parameters.length > 3) {
			longparamclasses++;
			r += "\n\t\tLong parameter list";
		}
		int effectivelines = 0, cyclomatic = 0;
		for (int l = start; l <= end; l++) {
			line = lines.get(l);
			if (!line.isBlank()) {
				effectivelines++;
				if (line.trim().startsWith("for ") || line.trim().startsWith("while ")
						|| line.trim().startsWith("if ")) {
					cyclomatic++;
				}
			}
		}
		if (effectivelines > 75) {
			largeclasses++;
			r += "\n\t\tLarge class";
		}
		if (effectivelines < 5) {
			freeloaderclasses++;
			r += "\n\t\tFree loader";
		}
		if (cyclomatic > 15) {
			cyclocomplex++;
			r += "\n\t\tCyclomatic Complex";
		}
		if (!r.contains("\n")) {
			return "";
		}
		return r + "\n\n";
	}

	private static List<int[]> getMethodBlocks(Map<Integer, String> lines) {
		List<int[]> r = new ArrayList<>();
		int indent = 0;
		for (int i = 1; i <= lines.size(); i++) {
			String line = lines.get(i);
			if (line.trim().length() > 0 && line.indexOf(line.trim()) < indent) {
				if (!r.isEmpty()) {
					int[] temp = r.get(r.size() - 1);
					int l = i - 1;
					while (lines.get(l).isBlank()) {
						l--;
					}
					temp[1] = l;
					;
					r.set(r.size() - 1, temp);
					System.out.println(Arrays.toString(temp));
				}
			}
			if (line.trim().startsWith("def ")) {
				r.add(new int[] { i, 0 });
				indent = line.indexOf(line.trim()) + 4;
			}
			if (i == lines.size() && !r.isEmpty() && r.get(r.size() - 1)[1] == 0) {
				int[] temp = r.get(r.size() - 1);
				int l = i;
				while (lines.get(l).isBlank()) {
					l--;
				}
				temp[1] = l;
				r.set(r.size() - 1, temp);
				System.out.println(Arrays.toString(temp));
			}
		}
		return r;
	}

	public static String findMethodSmells(Map<Integer, String> lines, int start, int end) {
		String r = "\t" + start + " ", line = lines.get(start++).trim();
		while (!line.endsWith(":")) {
			line += lines.get(start++).trim();
		}
		String name = line.split(" |\\(|:")[1];
		System.out.println(name);
		r += name;
		if (name.length() < 3 || name.length() > 25) {
			badidentifiermethods++;
			r += "\n\t\tBad identifier";
		}
		String[] parameters = line.indexOf("(") > -1
				? line.substring(line.indexOf("(") + 1, line.indexOf(")")).split(", *")
				: new String[0];
		System.out.println(parameters.length);
		if (parameters.length > 5) {
			longparammethods++;
			r += "\n\t\tLong parameter list";
		}
		int effectivelines = 0;
		for (int l = start; l <= end; l++) {
			line = lines.get(l);
			if (!line.isBlank()) {
				effectivelines++;
			}
		}
		if (effectivelines > 25) {
			longmethods++;
			r += "\n\t\tLong method";
		}
		if (effectivelines < 3) {
			freeloadermethods++;
			r += "\n\t\tFree loader";
		}
		if (!r.contains("\n")) {
			return "";
		}
		return r + "\n\n";
	}

	public static String countTotals() {
		String r = "Totals:\nGod lines: " + godlines;
		r += "\nClass smells:\n\tBad identifiers: " + badidentifierclasses;
		r += "\n\tLong parameter lists: " + longparamclasses;
		r += "\n\tLarge classes: " + largeclasses;
		r += "\n\tFree loaders: " + freeloaderclasses;
		r += "\n\tCyclomatic Complexes: " + cyclocomplex;
		r += "\nMethod smells:\n\tBad identifiers: " + badidentifiermethods;
		r += "\n\tLong parameter lists: " + longparammethods;
		r += "\n\tLong methods: " + longmethods;
		r += "\n\tFree loaders: " + freeloadermethods;
		return r;
	}
}
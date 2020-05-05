package search;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class Main {
    public static void main(String[] args) throws FileNotFoundException {
        /* This is for running from command line: java search.Main --data filename.txt
        List<String> people = readFile(args[0], args[1]); */

        List<String> people = readFile("--data", "Simple Search Engine/task/src/search/names.txt");
        Map<String, HashSet<Integer>> invertedIndex = makeInvertedIndex(people);
        menu(people, invertedIndex);
    }

    // read text file into arraylist people
    public static List<String> readFile(String args0, String args1) throws FileNotFoundException {
        List<String> people = new ArrayList<>();

        // read file
        if ("--data".equals(args0)) {
            Scanner input = new Scanner(new File(args1));

            // add people to ArrayList
            while (input.hasNextLine()) {
                people.add(input.nextLine());
            }
            input.close();
        }
        return people;
    }

    // read arraylist people into inverted index map
    public static Map<String, HashSet<Integer>> makeInvertedIndex(List<String> people) {

        // create inverted index
        // key: word lowercase, value: index of lines which contain word
        Map<String, HashSet<Integer>> invertedIndex = new HashMap<>();

        for (int i = 0; i < people.size(); i++) {
            String[] line = people.get(i).trim().split("\\s+");
            for (String s : line) {
                if (invertedIndex.containsKey(s.toLowerCase())) {
                    invertedIndex.get(s.toLowerCase()).add(i);
                } else {
                    invertedIndex.put(s.toLowerCase(), new HashSet<Integer>(Arrays.asList(i)));
                }
            }
        }
        return invertedIndex;
    }

    // MENU
    public static void menu(List<String> people, Map<String, HashSet<Integer>> invertedIndex) {
        Scanner console = new Scanner(System.in);
        String menu;
        while (true) {
            System.out.println("\n=== Menu ===\n" +
                    "1. Search information.\n" +
                    "2. Print all data.\n" +
                    "0. Exit.");
            menu = console.next();

            switch (menu) {
                case ("1"): // search
                    System.out.println("Select a matching strategy: ALL, ANY, NONE");
                    String strategy = console.next().toUpperCase();
                    Searcher searcher = new Searcher();
                    switch (strategy) {
                        case "ALL":
                            searcher.setMethod(new SearchALL());
                            break;
                        case "ANY":
                            searcher.setMethod(new SearchANY());
                            break;
                        case "NONE":
                            searcher.setMethod(new SearchNONE());
                            break;
                        default:
                            System.out.println("Invalid search strategy.");
                            break;
                    }

                    // search and print matches
                    List<String> matches = searcher.search(people, invertedIndex);
                    if (matches.isEmpty()) {
                        System.out.println("No matching people found.");
                    } else {
                        System.out.printf("%d persons found.\n", matches.size());
                        for (String person : matches) {
                            System.out.println(person);
                        }
                    }
                    break;
                case ("2"): // print people
                    System.out.println("=== List of people ===");
                    for (String person : people) {
                        System.out.println(person);
                    }
                    break;
                case ("0"): // exit
                    System.out.println("\nBye!");
                    return;
                default:
                    System.out.println("\nIncorrect option! Try again.");
            }
        }
    }
}

/**
 * Searcher represents a general searching method
 */
class Searcher {
    private SearchMethod method;

    public void setMethod(SearchMethod method) {
        this.method = method;
    }

    public List<String> search(List<String> people, Map<String, HashSet<Integer>> invertedIndex) {
        return this.method.search(people, invertedIndex);
    }
}

interface SearchMethod {

    List<String> search(List<String> people, Map<String, HashSet<Integer>> invertedIndex);
}

/**
 * Search strategy ALL prints lines containing all words from the query.
 */
class SearchALL implements SearchMethod {

    @Override
    public List<String> search(List<String> people, Map<String, HashSet<Integer>> invertedIndex) {
        Scanner console = new Scanner(System.in);
        System.out.println("\nEnter a name or email to search all suitable people.");
        String[] targetLine = console.nextLine().trim().toLowerCase().split("\\s+");
        List<String> matches = new ArrayList<>();

        // lookup search words in map, add index matches to list
        // match first word
        String target = targetLine[0];
        if (invertedIndex.containsKey(target)) {
            for (Integer i : invertedIndex.get(target)) {
                matches.add(people.get(i));
            }
        }

        // check other words against matched indexes in people
        List<String> removeList = new ArrayList<>();
        for (int i = 1; i < targetLine.length; i++) {
            for (String match : matches) {
                if (!match.toLowerCase().contains(targetLine[i])) {
                    removeList.add(match);
                }
            }
        }

        for (String remove : removeList) {
            matches.remove(remove);
        }

        return matches;
    }
}

/**
 * Search ANY strategy prints lines containing at least one word from the query
 */
class SearchANY implements SearchMethod {
    @Override
    public List<String> search(List<String> people, Map<String, HashSet<Integer>> invertedIndex) {
        Scanner console = new Scanner(System.in);
        System.out.println("\nEnter a name or email to search all suitable people.");
        String[] targetLine = console.nextLine().trim().toLowerCase().split("\\s+");
        List<String> matches = new ArrayList<>();

        // lookup search words in map, add index matches to list
        for (String target : targetLine) {
            if (invertedIndex.containsKey(target)) {
                for (Integer i : invertedIndex.get(target)) {
                    matches.add(people.get(i));
                }
            }
        }

        return matches;
    }
}

/**
 * Search NONE prints lines that do not contain words from the query at all
 */
class SearchNONE implements SearchMethod {

    @Override
    public List<String> search(List<String> people, Map<String, HashSet<Integer>> invertedIndex) {
        Scanner console = new Scanner(System.in);
        System.out.println("\nEnter a name or email to search all suitable people.");
        String[] targetLine = console.nextLine().trim().toLowerCase().split("\\s+");
        List<String> matches = new ArrayList<>();
        matches.addAll(people);

        // lookup search words in map, remove index matches to list
        for (String target : targetLine) {
            if (invertedIndex.containsKey(target)) {
                for (Integer i : invertedIndex.get(target)) {
                    matches.remove(people.get(i));
                }
            }
        }

        return matches;
    }
}


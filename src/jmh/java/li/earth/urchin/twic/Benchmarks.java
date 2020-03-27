package li.earth.urchin.twic;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Benchmarks {

    private static Random newRandom(String seasoning) {
        return new Random(Benchmarks.class.getName().hashCode() + seasoning.hashCode());
    }

    private static final String characters = "abcdefghijklmnopqrstuvwxyz";
    private static final int stringCount = 1009; // must be prime so that striding works
    private static int prefixLength = 3;
    private static int stringLength = 10;
    private static float matchingFraction = 0.1f;

    private static final List<String> allStrings;

    private static final String targetPrefix;
    private static final Pattern targetPrefixPattern;

    private static final Set<String> targetSet;
    private static final Pattern targetSetPattern;
    private static final Pattern targetSetPatternSorted;
    private static final Pattern targetSetPatternMinimal;

    static {
        Random random = newRandom("strings");

        targetPrefix = randomCharacters(prefixLength, random).collect(Collectors.joining());

        String notPrefix = targetPrefix.substring(0, targetPrefix.length() - 1) + randomCharacter(random);
        if (notPrefix.equals(targetPrefix)) throw new IllegalStateException("pick another seed!");

        int matchingStringCount = (int) (matchingFraction * stringCount);

        allStrings = Stream.concat(IntStream.range(0, matchingStringCount)
                                            .mapToObj(i -> randomCharacters(stringLength, random).collect(Collectors.joining("", targetPrefix, ""))),
                                   IntStream.range(matchingStringCount, stringCount)
                                            .mapToObj(i -> randomCharacters(stringLength, random).collect(Collectors.joining("", notPrefix, ""))))
                           .collect(Collectors.toList());
        if (new HashSet<>(allStrings).size() != stringCount) throw new IllegalStateException("pick another seed!");

        Collections.shuffle(allStrings, random);

        targetSet = new HashSet<>(allStrings.subList(0, matchingStringCount));

        targetPrefixPattern = Pattern.compile(targetPrefix + ".*");

        targetSetPattern = Pattern.compile(targetSet.stream().collect(Collectors.joining("|", "(?:", ")")));
        targetSetPatternSorted = Pattern.compile(targetSet.stream().sorted().collect(Collectors.joining("|", "(?:", ")")));
        targetSetPatternMinimal = Pattern.compile(PatternBuilder.minimalPattern(targetSet));
    }

    private static Stream<String> randomCharacters(int count, Random random) {
        return IntStream.range(0, count).mapToObj(i -> randomCharacter(random));
    }

    private static String randomCharacter(Random random) {
        int index = random.nextInt(characters.length());
        return characters.substring(index, index + 1);
    }

    @State(Scope.Thread)
    public static class IndexIterator {
        private final int step;
        private int index;

        public IndexIterator() {
            Random random = newRandom(Thread.currentThread().getName());
            step = random.nextInt(stringCount - 1) + 1;
            index = random.nextInt(stringCount);
        }

        int nextIndex() {
            index = (index + step) % stringCount;
            return index;
        }

        boolean nextBoolean(int oddsOfTrue) {
            return nextIndex() % oddsOfTrue == 0;
        }

        <E> E nextElement(List<E> list) {
            return list.get(nextIndex());
        }
    }

    /**
     * Negative control.
     */
    @Benchmark
    public boolean nothing(IndexIterator indexes) {
        return indexes.nextElement(allStrings) != null;
    }

    @Benchmark
    public boolean prefix(IndexIterator indexes) {
        return indexes.nextElement(allStrings).startsWith(targetPrefix);
    }

    @Benchmark
    public boolean prefixPattern(IndexIterator indexes) {
        return targetPrefixPattern.matcher(indexes.nextElement(allStrings)).matches();
    }

    @Benchmark
    public boolean set(IndexIterator indexes) {
        return targetSet.contains(indexes.nextElement(allStrings));
    }

    @Benchmark
    public boolean setPattern(IndexIterator indexes) {
        return targetSetPattern.matcher(indexes.nextElement(allStrings)).matches();
    }

    @Benchmark
    public boolean setPatternSorted(IndexIterator indexes) {
        return targetSetPatternSorted.matcher(indexes.nextElement(allStrings)).matches();
    }

    @Benchmark
    public boolean setPatternMinimal(IndexIterator indexes) {
        return targetSetPatternMinimal.matcher(indexes.nextElement(allStrings)).matches();
    }

}

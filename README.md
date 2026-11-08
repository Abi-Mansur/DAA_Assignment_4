DAA Assignment 4

This project implements recursive, full-table, and two-row Edit Distance, plus Naive and KMP string search. The benchmark compares elapsed time and operation counts.

 Open in IntelliJ IDEA

## Build and test

- Double-click **test** to run the JUnit 5 tests. The suite checks reference values, edge cases, and random inputs.

## Run the experiment

1. Open **Run → Edit Configurations** and add an **Application** configuration.
2. Set **Main class** to `BenchmarkRunner` and use the project module. Set **Working directory** to the folder containing `pom.xml`.
3. Enter `results-new` in **Program arguments** to save a fresh run separately from the submitted data. Save the configuration and click **Run**.
4. Там есть класс ReferenceChecker который проверяет сходства с referenced values 

The runner uses seed `20261108`, two warm-up runs, and five measured runs per case. It writes 480 measurements to `results-new/results.csv` and the median times to `results-new/summary.csv`.

## Project files

- `src/main/java/` contains the algorithms, counters, input generator, and benchmark runner.
- `src/test/java/` contains the JUnit 5 tests.
- `results/results.csv` contains the submitted measurements.




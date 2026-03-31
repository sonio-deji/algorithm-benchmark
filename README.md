# Algorithm Benchmarking Framework
### COSC 742 – Group 1: Searching Algorithms

---

## Project Structure

```
BenchmarkFramework/
├── src/
│   ├── Main.java                          ← Entry point
│   ├── algorithms/
│   │   ├── LinearSearch.java              ← O(n) array search
│   │   ├── LinkedListSearch.java          ← O(n) linked list search
│   │   └── BinarySearchTree.java          ← O(log n) avg BST search
│   └── framework/
│       ├── InputGenerator.java            ← Dataset generation
│       ├── BenchmarkEngine.java           ← Timing + memory measurement
│       ├── BenchmarkResult.java           ← Result data class
│       ├── ComplexityAnalyzer.java        ← R² curve fitting
│       ├── ResultsDatabase.java           ← CSV persistence
│       └── VisualizationEngine.java       ← HTML report generator
├── run.sh                                 ← One-command build & run
└── README.md
```

---

## How to Run

### Option A – Shell script (Linux/Mac)
```bash
chmod +x run.sh
./run.sh
```

### Option B – Manual (Windows / any OS)
```bash
# Compile
mkdir bin
javac -d bin -sourcepath src $(find src -name "*.java")

# Run
cd bin
java -Xmx512m Main
```

### Requirements
- Java 17+ (uses text blocks; compile with `--enable-preview` on Java 14-16)

---

## Output Files

| File | Description |
|------|-------------|
| `benchmark_results.csv` | Raw data: time, memory per algorithm per input size |
| `benchmark_report.html` | Self-contained HTML with 4 Chart.js graphs |

Open `benchmark_report.html` directly in any browser — no server needed.

---

## Algorithms Benchmarked

| Algorithm | Data Structure | Theoretical Complexity |
|-----------|---------------|----------------------|
| Linear Search | Array | O(n) time, O(1) space |
| Linked List Search | Singly Linked List | O(n) time, O(n) space |
| BST Search | Binary Search Tree | O(log n) avg, O(n) worst |

---

## Framework Components

### BenchmarkEngine
- **5 warm-up rounds** before measurement (eliminates JIT compilation bias)
- **20 measurement rounds** per (algorithm, input size) pair
- Reports mean, min, max, standard deviation

### Input Sizes
`n = 100 | 1,000 | 5,000 | 10,000 | 50,000 | 100,000`

### ComplexityAnalyzer
Uses OLS linear regression with transformed x-axes to estimate best-fit complexity:
- O(n): regress T against n
- O(n log n): regress T against n·log₂n
- O(n²): regress T against n²

Best fit = highest R² coefficient.

### VisualizationEngine
Generates a dark-themed HTML report with:
1. **Runtime Growth Graph** – line chart per algorithm
2. **Memory Consumption Graph** – bar chart per algorithm
3. **Algorithm Comparison** – multi-line overlay
4. **Empirical vs Theoretical** – measured data overlaid with O(n), O(log n), O(n²) curves

---

## Research Questions Addressed

| RQ | How |
|----|-----|
| RQ1: Effect of data structure on runtime | Comparing all three algorithms at same n |
| RQ2: Do empirical results match Big-O? | ComplexityAnalyzer R² fitting + Graph 4 |
| RQ3: Time-memory trade-offs | Graph 2 (memory) vs Graph 1 (time) |
| RQ4: Framework for CS education | Automated pipeline — run once, get full analysis |

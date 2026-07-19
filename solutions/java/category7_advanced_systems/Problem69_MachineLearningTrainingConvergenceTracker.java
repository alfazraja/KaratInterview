package category7_advanced_systems;

import java.util.*;

/**
 * Problem 69: Machine Learning Training Convergence Tracker
 * 
 * Three-part progression:
 * - Part 1: Handle floating-point precision in gradient calculations
 * - Part 2: Track convergence metrics (loss, accuracy)
 * - Part 3: Detect vanishing gradient and early stopping conditions
 */
public class Problem69_MachineLearningTrainingConvergenceTracker {
    
    static class TrainingMetrics {
        int epoch;
        double loss;
        double accuracy;
        double learningRate;
        long timestamp;
        
        TrainingMetrics(int epoch, double loss, double accuracy, double lr) {
            this.epoch = epoch;
            this.loss = loss;
            this.accuracy = accuracy;
            this.learningRate = lr;
            this.timestamp = System.currentTimeMillis();
        }
    }
    
    /**
     * Part 1: Bug Fix - Handle Floating-Point Precision
     * Avoid accumulation errors in gradient calculations
     */
    public static double calculateGradient(double loss, double previousLoss, double epsilon) {
        // Use epsilon for numerical stability
        if (Math.abs(loss) < epsilon) return 0.0;
        
        double gradient = (loss - previousLoss);
        
        // Guard against division by very small numbers
        if (Math.abs(previousLoss) < epsilon) {
            return 0.0;
        }
        
        return gradient / (Math.abs(previousLoss) + epsilon);
    }
    
    /**
     * Part 2: Track Training Convergence
     */
    public static class ConvergenceMonitor {
        List<TrainingMetrics> metrics = new ArrayList<>();
        double bestLoss = Double.MAX_VALUE;
        int bestEpoch = 0;
        
        public void recordMetrics(TrainingMetrics m) {
            metrics.add(m);
            
            if (m.loss < bestLoss) {
                bestLoss = m.loss;
                bestEpoch = m.epoch;
            }
        }
        
        public boolean hasConverged(double lossThreshold, int windowSize) {
            if (metrics.size() < windowSize) return false;
            
            // Check if loss is stable in recent window
            List<Double> recentLosses = new ArrayList<>();
            for (int i = Math.max(0, metrics.size() - windowSize); i < metrics.size(); i++) {
                recentLosses.add(metrics.get(i).loss);
            }
            
            double mean = recentLosses.stream().mapToDouble(Double::doubleValue).average().orElse(0);
            double variance = 0.0;
            for (double loss : recentLosses) {
                variance += Math.pow(loss - mean, 2);
            }
            variance /= recentLosses.size();
            
            return variance < lossThreshold;
        }
        
        public double getLossTrend() {
            if (metrics.size() < 2) return 0.0;
            
            // Calculate trend over last 5 epochs
            int start = Math.max(0, metrics.size() - 5);
            double firstLoss = metrics.get(start).loss;
            double lastLoss = metrics.get(metrics.size() - 1).loss;
            
            return (lastLoss - firstLoss) / Math.max(1, metrics.size() - start - 1);
        }
    }
    
    /**
     * Part 3: Detect Problematic Conditions
     */
    public static class TrainingDiagnostics {
        
        public static boolean detectVanishingGradient(List<TrainingMetrics> metrics, 
                                                      double gradientThreshold) {
            if (metrics.size() < 2) return false;
            
            int smallGradientCount = 0;
            
            for (int i = 1; i < metrics.size(); i++) {
                double gradient = calculateGradient(
                    metrics.get(i).loss,
                    metrics.get(i - 1).loss,
                    1e-10
                );
                
                if (Math.abs(gradient) < gradientThreshold) {
                    smallGradientCount++;
                }
            }
            
            // If >50% of gradients are tiny, we have vanishing gradient
            return smallGradientCount > (metrics.size() * 0.5);
        }
        
        public static boolean shouldEarlyStop(List<TrainingMetrics> metrics,
                                              int patienceEpochs) {
            if (metrics.size() < patienceEpochs) return false;
            
            TrainingMetrics best = metrics.get(0);
            int epochsSinceBest = 0;
            
            for (TrainingMetrics m : metrics) {
                if (m.loss < best.loss) {
                    best = m;
                    epochsSinceBest = 0;
                } else {
                    epochsSinceBest++;
                }
            }
            
            return epochsSinceBest >= patienceEpochs;
        }
        
        public static String diagnoseTraining(List<TrainingMetrics> metrics) {
            if (metrics.isEmpty()) return "No metrics recorded";
            
            double firstLoss = metrics.get(0).loss;
            double lastLoss = metrics.get(metrics.size() - 1).loss;
            double trend = (lastLoss - firstLoss) / metrics.size();
            
            if (trend > 0.001) {
                return "WARNING: Loss is increasing (diverging)";
            } else if (Math.abs(trend) < 0.00001) {
                return "INFO: Loss is stable (converged)";
            } else if (trend < -0.001) {
                return "INFO: Loss is decreasing steadily";
            }
            
            return "NORMAL: Training in progress";
        }
    }
    
    public static void main(String[] args) {
        System.out.println("=== Problem 69: ML Training Convergence Tracker ===");
        
        // Test Part 1: Gradient calculation
        double gradient = calculateGradient(0.45, 0.50, 1e-10);
        System.out.println("Gradient: " + gradient);
        
        // Test Part 2: Convergence monitoring
        ConvergenceMonitor monitor = new ConvergenceMonitor();
        monitor.recordMetrics(new TrainingMetrics(1, 2.5, 0.3, 0.01));
        monitor.recordMetrics(new TrainingMetrics(2, 2.1, 0.4, 0.01));
        monitor.recordMetrics(new TrainingMetrics(3, 1.9, 0.5, 0.01));
        monitor.recordMetrics(new TrainingMetrics(4, 1.8, 0.55, 0.01));
        monitor.recordMetrics(new TrainingMetrics(5, 1.8, 0.56, 0.01));
        
        System.out.println("Best loss: " + monitor.bestLoss);
        System.out.println("Loss trend: " + String.format("%.6f", monitor.getLossTrend()));
        System.out.println("Converged: " + monitor.hasConverged(0.01, 3));
        
        // Test Part 3: Diagnostics
        List<TrainingMetrics> allMetrics = new ArrayList<>();
        allMetrics.add(new TrainingMetrics(1, 2.5, 0.3, 0.01));
        allMetrics.add(new TrainingMetrics(2, 2.1, 0.4, 0.01));
        allMetrics.add(new TrainingMetrics(3, 1.9, 0.5, 0.01));
        
        boolean shouldStop = TrainingDiagnostics.shouldEarlyStop(allMetrics, 5);
        System.out.println("Should early stop: " + shouldStop);
        
        String diagnosis = TrainingDiagnostics.diagnoseTraining(allMetrics);
        System.out.println("Diagnosis: " + diagnosis);
    }
}

import { useState } from 'react';
import {
  View,
  Text,
  StyleSheet,
  TouchableOpacity,
  Platform,
  ScrollView,
} from 'react-native';
import AndroidAuto from 'react-native-sportscar';

export default function App() {
  const [isRunning, setIsRunning] = useState(false);
  const [results, setResults] = useState<{
    resolved: number;
    rejected: number;
    notSettled: number;
    totalTime: number;
  } | null>(null);

  if (Platform.OS === 'ios') {
    return (
      <View style={styles.container}>
        <Text style={styles.errorText}>Only Android is supported on iOS</Text>
      </View>
    );
  }

  const createDummyDependency = (): any => ({
    callAsyncFunction: () => {
      return new Promise<boolean>((resolve, reject) => {
        // Call the Pokemon API instead of using timeout
        fetch('https://pokeapi.co/api/v2/pokemon/charizard')
          .then(response => {
            if (response.ok) {
              resolve(true);
            } else {
              reject(new Error(`HTTP ${response.status}`));
            }
          })
          .catch(error => {
            reject(error);
          });
      });
    },
  });

  const runParallel = async () => {
    if (isRunning) return;
    
    setIsRunning(true);
    setResults(null);
    
    const startTime = Date.now();
    const promises: Promise<boolean>[] = [];
    
    // Create 1000 promises
    for (let i = 0; i < 1000; i++) {
      const promise = AndroidAuto.callDummyPromiseFunction(createDummyDependency());
      promises.push(promise);
    }
    
    // Wait for 10 seconds, then check results
    setTimeout(async () => {
      let resolved = 0;
      let rejected = 0;
      let notSettled = 0;
      
      // Use Promise.allSettled to check all promises
      const settledResults = await Promise.allSettled(promises);
      
      settledResults.forEach((result) => {
        if (result.status === 'fulfilled') {
          resolved++;
        } else if (result.status === 'rejected') {
          rejected++;
        }
      });
      
      // Calculate not settled as promises that didn't complete in time
      notSettled = 1000 - resolved - rejected;
      
      const totalTime = Date.now() - startTime;
      setResults({ resolved, rejected, notSettled, totalTime });
      setIsRunning(false);
    }, 10000);
  };

  const runSequential = async () => {
    if (isRunning) return;
    
    setIsRunning(true);
    setResults(null);
    
    const startTime = Date.now();
    let resolved = 0;
    let rejected = 0;
    let notSettled = 0;


    for (let i = 0; i < 1000; i++) {
    
      
      try {
        const promise = await Promise.race([AndroidAuto.callDummyPromiseFunction(createDummyDependency()), new Promise((_resolve, _reject) => setTimeout(() => _resolve(false), 2000))]);
        if(promise) {
          resolved++;
        } else {
          notSettled++;
        }
      } catch (error) {
        console.log('error', error);
        rejected++;
      }
    }

  
    
    const totalTime = Date.now() - startTime;
    setResults({ resolved, rejected, notSettled, totalTime });
    setIsRunning(false);
  };

  return (
    <ScrollView style={styles.container}>
      <View style={[styles.header, isRunning && styles.headerRunning]}>
        <Text style={styles.title}>Nitro Promise Bridge Test</Text>
        <Text style={styles.subtitle}>
          {isRunning ? '🔄 Test in Progress...' : 'Testing promise settling through Nitro bridge (10s timeout)'}
        </Text>
      </View>

      <View style={styles.buttonContainer}>
        <TouchableOpacity
          style={[styles.button, styles.parallelButton, isRunning && styles.disabledButton]}
          onPress={runParallel}
          disabled={isRunning}
        >
          <Text style={styles.buttonText}>Run Parallel (1000 calls)</Text>
        </TouchableOpacity>

        <TouchableOpacity
          style={[styles.button, styles.sequentialButton, isRunning && styles.disabledButton]}
          onPress={runSequential}
          disabled={isRunning}
        >
          <Text style={styles.buttonText}>Run Sequential (1000 calls)</Text>
        </TouchableOpacity>
      </View>

      {isRunning && (
        <View style={styles.runningContainer}>
          <Text style={styles.runningText}>⏳ Test Running...</Text>
          <Text style={styles.runningSubtext}>Please wait 10 seconds for results</Text>
          <View style={styles.progressBar}>
            <View style={styles.progressFill} />
          </View>
        </View>
      )}

      {results && (
        <View style={styles.resultsContainer}>
          <Text style={styles.resultsTitle}>📊 Nitro Bridge Results</Text>
          <View style={styles.resultRow}>
            <Text style={styles.resultLabel}>✅ Settled (Resolved):</Text>
            <Text style={[styles.resultValue, styles.resolvedText]}>{results.resolved}</Text>
          </View>
          <View style={styles.resultRow}>
            <Text style={styles.resultLabel}>❌ Settled (Rejected):</Text>
            <Text style={[styles.resultValue, styles.rejectedText]}>{results.rejected}</Text>
          </View>
          <View style={styles.resultRow}>
            <Text style={styles.resultLabel}>⏳ Not Settled (Bridge Issue):</Text>
            <Text style={[styles.resultValue, styles.notSettledText]}>{results.notSettled}</Text>
          </View>
          <View style={styles.resultRow}>
            <Text style={styles.resultLabel}>⏱️ Total Time:</Text>
            <Text style={styles.resultValue}>{results.totalTime}ms</Text>
          </View>
          <View style={styles.resultRow}>
            <Text style={styles.resultLabel}>📈 Total Calls:</Text>
            <Text style={styles.resultValue}>{results.resolved + results.rejected + results.notSettled}</Text>
          </View>
        </View>
      )}
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f5f5f5',
  },
  header: {
    backgroundColor: '#2196F3',
    padding: 20,
    paddingTop: 60,
    alignItems: 'center',
  },
  headerRunning: {
    backgroundColor: '#1976D2',
  },
  title: {
    fontSize: 24,
    fontWeight: 'bold',
    color: 'white',
    marginBottom: 5,
  },
  subtitle: {
    fontSize: 16,
    color: 'rgba(255, 255, 255, 0.8)',
  },
  errorText: {
    fontSize: 18,
    color: 'red',
    textAlign: 'center',
    marginTop: 100,
  },
  buttonContainer: {
    padding: 20,
    gap: 15,
  },
  button: {
    paddingVertical: 15,
    paddingHorizontal: 20,
    borderRadius: 8,
    alignItems: 'center',
    shadowColor: '#000',
    shadowOffset: {
      width: 0,
      height: 2,
    },
    shadowOpacity: 0.25,
    shadowRadius: 3.84,
    elevation: 5,
  },
  parallelButton: {
    backgroundColor: '#4CAF50',
  },
  sequentialButton: {
    backgroundColor: '#FF9800',
  },
  disabledButton: {
    backgroundColor: '#ccc',
  },
  buttonText: {
    color: 'white',
    fontSize: 16,
    fontWeight: 'bold',
  },
  runningContainer: {
    backgroundColor: '#E3F2FD',
    padding: 25,
    margin: 20,
    borderRadius: 12,
    alignItems: 'center',
    borderWidth: 2,
    borderColor: '#2196F3',
    shadowColor: '#000',
    shadowOffset: {
      width: 0,
      height: 2,
    },
    shadowOpacity: 0.25,
    shadowRadius: 3.84,
    elevation: 5,
  },
  runningText: {
    fontSize: 20,
    color: '#1976D2',
    fontWeight: 'bold',
    marginBottom: 5,
  },
  runningSubtext: {
    fontSize: 14,
    color: '#1976D2',
    marginBottom: 15,
  },
  progressBar: {
    width: '100%',
    height: 8,
    backgroundColor: '#BBDEFB',
    borderRadius: 4,
    overflow: 'hidden',
  },
  progressFill: {
    height: '100%',
    backgroundColor: '#2196F3',
    borderRadius: 4,
    width: '100%',
    // Animation would be added here if needed
  },
  resultsContainer: {
    backgroundColor: 'white',
    margin: 20,
    padding: 20,
    borderRadius: 8,
    shadowColor: '#000',
    shadowOffset: {
      width: 0,
      height: 2,
    },
    shadowOpacity: 0.25,
    shadowRadius: 3.84,
    elevation: 5,
  },
  resultsTitle: {
    fontSize: 20,
    fontWeight: 'bold',
    color: '#333',
    marginBottom: 15,
    textAlign: 'center',
  },
  resultRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingVertical: 8,
    borderBottomWidth: 1,
    borderBottomColor: '#f0f0f0',
  },
  resultLabel: {
    fontSize: 16,
    color: '#666',
    flex: 1,
  },
  resultValue: {
    fontSize: 16,
    fontWeight: 'bold',
    color: '#333',
  },
  resolvedText: {
    color: '#4CAF50',
  },
  rejectedText: {
    color: '#F44336',
  },
  notSettledText: {
    color: '#FF9800',
  },
});

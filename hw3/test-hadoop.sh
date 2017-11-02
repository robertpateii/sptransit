echo "TEST SCRIPT: Assuming input/file exists with words in 'file'"
echo "TEST SCRIPT: Deleting local output folder assuming it's leftover from last test"
rm -r output
hdfs dfs -copyFromLocal input/ /input # copy the local directory to HDFS
echo "TEST SCRIPT: Copied local input to DFS. Here's the files on HDFS:"
hdfs dfs -ls /input
echo "TEST SCRIPT: Kicking off the test..."
hadoop jar wc.jar CountingIndexer /input /output
echo "TEST SCRIPT: Done with test. Copying over output then cleaning up"
hdfs dfs -ls /output # you should see all the outputs
hdfs dfs -copyToLocal /output # download the result from HDFS
hdfs dfs -rm -R /input
hdfs dfs -rm -R /output

void testcase2() {
    int cachePower = 24; // cache size = 2^16B
    int blockPower = 6; // block size = 2^5B
    int N = 4096;
    double[] y = new double[N];
    double[][] x = new double[N][N];
    double[][] a = new double[N][N];
    String cacheType = "DirectMapped";
    for(int k = 0; k < N; k+=1){
   	for (int j = 0; j < N; j+=1){
	    for (int i = 0; i < N; i+=1){
		y[i] = y[i] + a[i][j] * x[k][j];
	    }
	}
    }
}


void testcase1() {
    int cachePower = 18; // cache size = 2^16B
    int blockPower = 5; // block size = 2^5B
    int setSize = 4;
    int N = 32768;
    int stride = 32768;
    int strideT = 327680;
    double s = 0.0;
    double[] A = new double[N];
    String cacheType = "SetAssociative";

    for (int it = 0; it < strideT;i+=1){
        for (int i = 0; i < N; i+=stride){
		s += A[i];
	}
    }
}


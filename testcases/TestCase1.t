void testcase1() {
    int cachePower = 16; // cache size = 2^16B
    int blockPower = 5; // block size = 2^5B
    int setSize = 8;
    int stride = 1;
    int N = 1024;
    long[] A = new long[N];
    String cacheType = "SetAssociative";

    for (int i = 0;i < N;i+=1){
        A[i] = 0;
    }
}


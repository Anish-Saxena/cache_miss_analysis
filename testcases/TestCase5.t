void testcase3() {
    int cachePower = 17; // cache size = 2^18B
    int blockPower = 5; // block size = 2^6B
    int N = 512;
    int[][] A = new int[N][N];
    int[][] B = new int[N][N];
    int[][] C = new int[N][N];
    String cacheType = "DirectMapped";
    for (int i = 0; i < N; i += 1) {
        for (int k = 0; k < N; k += 1) {
            for (int j = 0; j < N; j += 1) {
                C[i][j] += A[i][k] * B[k][j];
            }
        }
    }
}


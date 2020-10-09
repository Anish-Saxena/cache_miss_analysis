void testcase1() {
    int cachePower = 16; // cache size = 2^16B
    int blockPower = 5; // block size = 2^5B
    int setSize = 8;
    int[] A = new int[65536];
    String cacheType = "SetAssociative";
    for (int i = 0; i < 32768; i += 64){
	for (int j = 0; j < 65536; j += 1){
	    A[j] += 1;
	}
    }
}


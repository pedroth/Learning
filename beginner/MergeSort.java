package beginner;

import java.util.Random;

public class MergeSort {

	public static void mergeSort(int v[], int p, int r) {
		if (p < r) {
			int q = (int) Math.floor((p + r) / 2);
			mergeSort(v,p,q);
			mergeSort(v, q+1, r);
			merge(v,p,q,r);
		}
	}
	
	public static void merge(int v[], int p, int q, int r){
		int[] l = new int[q - p];
	}

	public static void main(String[] args) {
		Random rand = new Random();
		int size = rand.nextInt();
		int v[] = new int[size];
		for (int i = 0; i < size; i++)
			v[i] = rand.nextInt(size);
		MergeSort.mergeSort(v, 0, size - 1);
		for(int k : v)
			System.out.println(k);
	}
}

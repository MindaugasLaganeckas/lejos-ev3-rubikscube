package ev3.rubikscube.controller;

import cs.min2phase.Search;

public class Main {

	public static void main(String[] args) {
		final String scrambledCube = 
				  "WRGOOOOOG" //U
				+ "WGYYWWWYY" //R
				+ "WYRBGGYWB" //F
				+ "RROGRRYRO" //D
				+ "BBGWYYGWB" //L
				+ "RBRBBOBGO"; //B
		final String result = new Search().solution(scrambledCube, 21, 100000000, 10000, 0);
        System.out.println(result);
	}
}

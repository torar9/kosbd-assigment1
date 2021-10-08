package cz.osu.ukol;

import lombok.Getter;

import java.util.stream.Stream;

public class Calculator {
    @Getter final private int[][] GMatrix;
    @Getter private int[][] HMatrix;
    @Getter final private CodeType code;
    @Getter private int minHammingLen;

    Calculator(int[][] GMatrix, CodeType code) {
        this.GMatrix = GMatrix;
        this.code = code;
        calcHMatrix();
        calcHammingLen();
    }

    /**
     * Zakóduje bitovou zprávu.
     * @param message zpráva složená z bitů
     * @return Vrátí zakódovanou zprávu
     */
    public String encode(String message) {
        int tmp[][] = new int[1][code.getNumberOfInfoBits()];

        tmp[0] = Stream.of(message.split(""))
                .mapToInt(Integer::parseInt)
                .toArray();

        return arrayToString(multiplyMatrix(tmp, GMatrix));
    }

    /**
     * Vytvoří kontrolní matici H
     */
    private void calcHMatrix() {
        HMatrix = new int[code.getRedundancy()][code.getLength()];

        for(int i = 0; i < code.getNumberOfInfoBits(); i++) {
            for(int j = code.getNumberOfInfoBits(); j < code.getLength(); j++) {
                if(j >= code.getNumberOfInfoBits())
                    HMatrix[j - code.getNumberOfInfoBits()][i] = GMatrix[i][j];
            }
        }

        for(int i = 0; i < code.getRedundancy(); i++) {
            for(int j = code.getNumberOfInfoBits() ; j < code.getLength(); j++) {
                if(j - code.getNumberOfInfoBits() == i)
                    HMatrix[i][j] = 1;
                else
                    HMatrix[i][j] = 0;
            }
        }
    }

    /**
     * Vypočítá minimální Hammingovu vzdálenost d
     */
    private void calcHammingLen() {
        int result = 1000;

        for(int i = 0; i < code.getNumberOfInfoBits(); i++) {
            int tmpD = 0;
            for(int j = 0; j < code.getNumberOfInfoBits(); j++) {
                if(i == j)
                    continue;
                for(int x = 0; x < code.getLength(); x++){
                    int num1 = GMatrix[i][x];
                    int num2 = GMatrix[j][x];

                    if(num1 != num2)
                        tmpD++;
                }
                if(result > tmpD)
                    result = tmpD;
                tmpD = 0;
            }
        }

        minHammingLen = result;
    }

    /**
     * Pomocná metoda pro roznásobení matic k určení syndromu a zakódované zprávy
     * @param A
     * @param B
     * @return Vrátí roznásobenou matici
     */
    private int[][] multiplyMatrix(int A[][], int B[][])
    {
        int C[][] = new int[A.length][B[0].length];

        for (int i = 0; i < A.length; i++) {
            for (int j = 0; j < B[0].length; j++) {
                int tmp = 0;
                for (int k = 0; k < B.length; k++) {
                    if (A[i][k] == 1 && B[k][j] == 1)
                        tmp++;
                }

                if(tmp % 2 != 0)
                    C[i][j] = 1;
            }
        }

        return C;
    }

    private String arrayToString(int arr[][]) {
        String result = "";
        for(int i = 0; i < arr.length; i++) {
            for (int j = 0; j < arr[0].length; j++) {
                result += arr[i][j];
            }
        }
        return result;
    }
}

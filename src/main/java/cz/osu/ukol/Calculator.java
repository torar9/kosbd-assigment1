package cz.osu.ukol;

import lombok.Getter;
import org.ejml.dense.row.MatrixFeatures_DDRM;
import org.ejml.simple.SimpleMatrix;

import java.util.*;
import java.util.stream.Stream;

public class Calculator {
    @Getter final private double[][] GMatrix;
    @Getter private double[][] HMatrix;
    @Getter final private CodeType code;
    @Getter private int minHammingLen;
    private double[][] syndrom;

    Calculator(int[][] GMatrix, CodeType code) {
        this.GMatrix = intArrToDoubleArr(GMatrix);
        this.code = code;
        calcHMatrix();
        calcHammingLen();
    }

    /**
     * Zakóduje bitovou zprávu.
     * @param message zpráva k dekódování složená z bitů
     * @return Vrátí zakódovanou zprávu
     */
    public String encode(String message) {
        double tmp[][] = new double[1][code.getNumberOfInfoBits()];

        tmp[0] = Stream.of(message.split(""))
                .mapToDouble(Double::parseDouble)
                .toArray();

        return arrayToString(multiplyMatrix(tmp, GMatrix));
    }

    /**
     * Vypočítá syndrom
     * @param message Zakódovaná zpráva v bitech
     * @return Vypočítaný syndrom
     */
    public double calcSyndrome(String message) {
        double tmp[][] = new double[code.getLength()][1];

        double tmp2[] = Stream.of(message.split(""))
                .mapToDouble(Double::parseDouble)
                .toArray();

        for(int i = 0; i < tmp2.length; i++) {
            tmp[i][0] = tmp2[i];
        }

        syndrom = multiplyMatrix(HMatrix, tmp);

        return Double.parseDouble(arrayToString(syndrom));
    }

    /**
     * Zjistí zda se v kódě vyskytuje chyba podle syndromu.
     * V případě že ano, tak se zjistí pozice chybného bitu.
     * Pokud chybu nelze najít, tak pouze zaznamená že došlo k chybě.
     * @param input
     * @return
     */
    public CodeReport getCodeReport(String input) {
        if(syndrom == null)
            throw new NullPointerException("Syndrom is not set up");

        if(Integer.parseInt(arrayToString(syndrom)) == 0)
            return new CodeReport(false, true,-1);

        int counter = 0;
        int pos = -1;
        int j = 0;
        for(int i = 0; i < HMatrix[0].length; i++) {
            int tmp = 0;
            for(j = 0; j < HMatrix.length; j++) {
                if(HMatrix[j][i] == syndrom[j][0])
                    tmp++;
            }
            if(tmp == HMatrix.length) {
                pos = i + 1;
                counter++;
            }
            tmp = 0;
        }

        if(counter == 0) {
            return new CodeReport(true, false, -1);
        }

        return new CodeReport(true, true, pos);
    }

    /**
     * Dekóduje bitovou zprávu
     * @param message zpráva k zakódování složená z bitů
     * @return Vrátí dekódovanou zprávu
     */
    public String decode(String message) {
        return message.substring(0, code.getNumberOfInfoBits());
    }

    /**
     * Vytvoří kontrolní matici H
     */
    private void calcHMatrix() {
        HMatrix = new double[code.getRedundancy()][code.getLength()];

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
        int result = Integer.MAX_VALUE;

        for(int i = 0; i < code.getNumberOfInfoBits(); i++) {
            int tmpD = 0;
            for(int j = 0; j < code.getNumberOfInfoBits(); j++) {
                if(i == j)
                    continue;
                for(int x = 0; x < code.getLength(); x++) { // Kombinuji veškeré řádky
                    double num1 = GMatrix[i][x];
                    double num2 = GMatrix[j][x];

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
    private double[][] multiplyMatrix(double A[][], double B[][])
    {
        double C[][] = new double[A.length][B[0].length];

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

    /**
     * Pomocná třída pro převod pole do řetězce.
     * @param arr
     * @return
     */
    private String arrayToString(double arr[][]) {
        String result = "";
        for(int i = 0; i < arr.length; i++) {
            for (int j = 0; j < arr[0].length; j++) {
                result += (int)arr[i][j];
            }
        }
        return result;
    }

    private double[][] intArrToDoubleArr(int arr[][]) {
        double result[][] = new double[arr.length][arr[0].length];

        for (int i = 0; i < result.length; i++) {
            for (int j = 0; j < result[0].length; j++) {
                result[i][j] = (double)arr[i][j];
            }
        }
        return result;
    }

    private int[][] doubleArrToIntArr(double arr[][]) {
        int result[][] = new int[arr.length][arr[0].length];

        for (int i = 0; i < result.length; i++) {
            for (int j = 0; j < result[0].length; j++) {
                result[i][j] = (int)arr[i][j];
            }
        }
        return result;
    }

    public int calculateRank() {
        SimpleMatrix expResult = new SimpleMatrix(GMatrix);
        return MatrixFeatures_DDRM.rank(expResult.getDDRM());
    }
}

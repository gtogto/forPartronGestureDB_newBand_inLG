package com.example.android.bluetoothlegatt.com;

/**
 * Created by Ine on 2017-08-17.
 */

import android.util.Log;

public class GestureRecognition {




    private int [][] WMA_SENSOR = new int [AXIS_NUM][WMA_SAMPLE_NUM];
    private int [][] SENSOR_tmep =  new int [AXIS_NUM][SAMPLE_NUM];

    private int [][] WMA_temp = new int [AXIS_NUM][WMA_SAMPLE_NUM];

    private byte [] WEIGHTED_AXIS = new byte [AXIS_NUM];
    private byte Motionflag_ = 0;
    public int [] gaps = {0,0,0,0,0,0};
    private int [] biggestValue;
    private int result_;

    public boolean band_leaned = false;
    private void setBandLeaned (boolean band)   {
        this.band_leaned = band;
    }



    ///////////////////////////////////////////////////////////////////////
    public int gestureRecogInterface (int [][] SENSOR, boolean band_leaned_flag)	{
        ///////////////////////////////////////////////////////////////////////
        // A factor: The array of sensor data
        // Return: NULL
        //////////////////////////////////////////////////////////////////////
        result_ = UNKNOWN_;
        biggestValue = new int [GESTURE_NUM];
        int nLoop, nLoop2;

        //THIS LOOP IS INSERTED JUST FOR DEBUGING

        setBandLeaned (band_leaned_flag);


        /*
        if (band_leaned_flag) {
            Log.e(TAG, "BAND LEANED!");
        }
        else    {
            Log.e(TAG, "BAND STANDARD!");
        }
*/

        //GestureDB gesture;
        normalizingFunction(SENSOR);

        weightedMovingAverage(SENSOR_tmep);

/*  //TODO : Sensor data info
        Log.d (TAG, "NORNALIZED ");
        StringBuilder sb1 = new StringBuilder();
        for (nLoop = 0; nLoop < AXIS_NUM; nLoop ++)	{
            for (nLoop2 = 0; nLoop2 < SAMPLE_NUM; nLoop2 ++)	{
                //    Log.d(TAG, "[" + nLoop + "][" +nLoop2 + "] " + SENSOR [nLoop][nLoop2]);
                sb1.append(SENSOR_tmep [nLoop][nLoop2]);
                sb1.append (" ");
            }
            sb1.append ("\n");
        }
        Log.d(TAG, sb1.toString());

        Log.d (TAG, "WEIGHTED ");

        StringBuilder sb = new StringBuilder();
        sb.append("DATA: ");
        for (nLoop = 0; nLoop < AXIS_NUM; nLoop ++)	{
            for (nLoop2 = 0; nLoop2 < WMA_SAMPLE_NUM; nLoop2 ++)	{
                //    Log.d(TAG, "[" + nLoop + "][" +nLoop2 + "] " + SENSOR [nLoop][nLoop2]);
                sb.append(WMA_SENSOR [nLoop][nLoop2]);
                sb.append (" ");
            }
            sb.append ("\n");
        }
        Log.d(TAG, sb.toString());
*/

        validAxiesClassifier();

        result_ = groupClassifier();

        switch (result_)	{
            case DUMP:
                Log.d (TAG,"-------------------- DUMP--\n"); break;
            case LEFT:
                Log.i (TAG," <<<< LEFT <<< \n");    // 3rd is FRONT
                break;
            case RIGHT:
                Log.i (TAG," >>>> RIGHT >>\n");
                break;
            case FRONT:
            case BACK:
                result_ = FRONT;
                Log.i (TAG,"[[[ FRONT ]\n");        // // 3rd is UP or CLOCK
                break;
            case UP:
            case DOWN:
                result_ = UP;
                Log.i (TAG," ^^^^ UP\n");           // // 3rd is side
                break;
            case CLOCK:
                result_ = CLOCK;
                Log.i (TAG," **** CLOCK\n");
                break;
            case ANTI_CLOCK:

                result_ = ANTI_CLOCK;
                Log.i (TAG," **** ANTI CLOCK\n");
                break;
            case UNKNOWN_:
                Log.i (TAG," CAN'T DETECT\n"); 	break;
            default:
                Log.i (TAG," ELSE \n"); 	break;

        }	//switch



        return result_;
    } //function gestureRecogInterface



    ///////////////////////////////////////////////////////////////////////
    private void normalizingFunction (int [][] sensor){
        ///////////////////////////////////////////////////////////////////////

        int nLoop, nLoop2;

        //Arrays.fill(SENSOR_tmep, 0);

        for (nLoop = 0; nLoop < AXIS_NUM; nLoop ++)	{
            for (nLoop2 = 0; nLoop2 < SAMPLE_NUM; nLoop2 ++)	{

                SENSOR_tmep [nLoop][nLoop2] = (int)(((double)sensor [nLoop][nLoop2] / NORMALIZATION_FACTOR) * 1000);
                //Log.d(TAG, "["+nLoop+"/"+nLoop2+"]"+SENSOR_tmep [nLoop][nLoop2]);
            }
        }

    }

    ///////////////////////////////////////////////////////////////////////
    private void weightedMovingAverage (int [][] SENSOR)	{
        ///////////////////////////////////////////////////////////////////////

        int nLoop, nLoop2, nLoop3;
        int temp_avr;

        // Arrays.fill(WMA_SENSOR, 0);

        for (nLoop = 0; nLoop < AXIS_NUM; nLoop ++)	{
            for (nLoop2 = 0; nLoop2 < WMA_SAMPLE_NUM; nLoop2 ++)	{
                temp_avr = 0;
                for (nLoop3 = 0; nLoop3 < WMA_FACTOR; nLoop3 ++)	{
                    temp_avr +=  SENSOR [nLoop][nLoop2 + nLoop3];
                } //for nLoop3

                WMA_SENSOR [nLoop][nLoop2] = temp_avr / WMA_FACTOR;
            } //for nLoop2
        } //for nLoop
    }



    ///////////////////////////////////////////////////////////////////////
    private void validAxiesClassifier ()	{
        ///////////////////////////////////////////////////////////////////////
        // A factor: The array of sensor data
        // Return: NULL
        //////////////////////////////////////////////////////////////////////
        int [] min_ = {10000,10000,30000, 10000, 10000, 10000};
        int [] max_ = {-10000, -10000,-1000, -10000, -10000,-1000};
        int [] minFlag = {0,0,0,0,0,0};
        int [] maxFlag = {0,0,0,0,0,0};
        int nLoop, nLoop2;

        Motionflag_ = 0;

        char temp_flag = 0;
        for (nLoop = ACC_X; nLoop <= ACC_Z; nLoop++ )	{

            for (nLoop2 = 0; nLoop2 < WMA_SAMPLE_NUM; nLoop2 ++)	{

                if (min_[nLoop] > WMA_SENSOR [nLoop][nLoop2])	{
                    min_[nLoop] = WMA_SENSOR [nLoop][nLoop2];
                    minFlag [nLoop] |= 0x1;
                } else if (max_[nLoop] < WMA_SENSOR [nLoop][nLoop2])	{
                    max_[nLoop] = WMA_SENSOR [nLoop][nLoop2];
                    maxFlag [nLoop] |= 0x1;
                }
                minFlag[nLoop] = minFlag[nLoop] << 1;
                maxFlag[nLoop] = maxFlag[nLoop] << 1;

            } //for nLoop2

            gaps [nLoop] = max_ [nLoop] - min_ [nLoop];
            // Log.d (TAG, "gap is "+gaps [nLoop]);

            if (gaps [nLoop] > GAP_THRESHOLD)	{
                Motionflag_ |= 0x1;
                temp_flag ++;
                WEIGHTED_AXIS [nLoop] = 90;

            }

            Motionflag_ = (byte) (Motionflag_ << 1);

        } //for nLoop


        if (temp_flag == 3)	{
            int nLoop3 = 0, temp_min=2000, order = 0;
            for (nLoop3=0; nLoop3 < 3; nLoop3 ++ )	{

                if (gaps [nLoop3] < temp_min)	{
                    order = nLoop3;
                    temp_min = gaps [nLoop3];
                }

            }	//for nLoop3


            switch (order)	{
                case 0:
                    Motionflag_ = 0x6;
                    WEIGHTED_AXIS [order] = 10;
                    temp_flag --;
                    break;
                case 1:
                    Motionflag_ = 0xA;
                    WEIGHTED_AXIS [order] = 10;
                    temp_flag --;
                    break;
                case 2:
                    Motionflag_ = 0xC;
                    WEIGHTED_AXIS [order] = 10;
                    temp_flag --;
                    break;

            }	//switch
        }	//if

        if (temp_flag != 0) {
            for (nLoop = ACC_X; nLoop < AXIS_NUM; nLoop++) {
                WEIGHTED_AXIS[nLoop] /= temp_flag;
            }
        }

    }	//function validAxiesClassifier


    ///////////////////////////////////////////////////////////////////////
    private int groupClassifier ()	{
        ///////////////////////////////////////////////////////////////////////
        // A factor: The array of sensor data
        // Return: NULL
        //////////////////////////////////////////////////////////////////////
        result_ = DUMP;
        int nLoop;

        // Arrays.fill(biggestValue, 0);


        if (Motionflag_ == 0)	{
            result_ = DUMP;
        }
        else {

            int temp_result=0, max=0;

            // 4: Y-axis
            // 2: Z-axis
            // 8: X-axis
            switch (Motionflag_ & 0x0E)	{

                case 0x0:
                    result_ = DUMP;
                    break;
                case 0x8:	//FRONT
                    //Log.d(TAG, "flag is 8");
                    result_ = FRONT;
                    break;

                case 0x2:	//UP
                    // Log.d(TAG, "flag is 2");
                    if (band_leaned ) {

                        result_ = RIGHT;
                        result_ = assignDB_RIGHT_LEANED ();

//                    result_ = assignDB_FRONT ();
                    }
                    else {
                        result_ = UP;
                    }
                    break;
                case 0x4:		//SIDE
                    // Log.d(TAG, "flag is 4");

                    if (band_leaned ) {
                        result_ = UP;
                    }
                    else    {
                        result_ = RIGHT;
                        result_ = assignDB_RIGHT();
                    }
                    break;

                case 0x6:
                    // Log.d(TAG, "flag is 0x6");

                    if (gaps[0] + gaps[2] < 1900)	{
                        result_ = CLOCK;
                        temp_result = assignDB_CLOCK ();
                        if (band_leaned)    {
                            result_ = RIGHT;
                            temp_result = assignDB_RIGHT_LEANED ();
                            result_ = FRONT;
                            temp_result = assignDB_FRONT_LEANED ();
                        }
                        else {

                            result_ = FRONT;
                            temp_result = assignDB_FRONT();
                            result_ = RIGHT;
                            temp_result = assignDB_RIGHT();
                        }

                        for (nLoop = RIGHT; nLoop < GESTURE_NUM; nLoop ++)	{
                            if (max < biggestValue [nLoop])	{
                                max = biggestValue [nLoop];
                                result_ = nLoop;
                            }
                        }
                    }
                    else	{
                        result_ = CLOCK;
                        result_ = assignDB_CLOCK ();
                    }

                    break;

                case 0xA:
                    // Log.d(TAG, "flag is A");

                    if (band_leaned)    {
                        result_ = RIGHT;
                        temp_result = assignDB_RIGHT_LEANED ();
                        result_ = FRONT;
                        temp_result = assignDB_FRONT_LEANED ();

                        for (nLoop = FRONT; nLoop < GESTURE_NUM; nLoop ++)	{
                            //    Log.d(TAG, "value is " + biggestValue[nLoop]);
                            if (max < biggestValue [nLoop])	{
                                max = biggestValue [nLoop];
                                result_ = nLoop;
                            } //if
                        }


                    }
                    else {
                        result_ = FRONT;
                        result_ = assignDB_FRONT();

                    }

                    break;

                default:

                    result_ = CLOCK;
                    temp_result = assignDB_CLOCK ();

                    if (band_leaned ) {
                        result_ = RIGHT;
                        temp_result = assignDB_RIGHT_LEANED();
                        result_ = FRONT;
                        temp_result = assignDB_FRONT_LEANED();
                    }
                    else    {
                        result_ = RIGHT;
                        temp_result = assignDB_RIGHT ();
                        result_ = FRONT;
                        temp_result = assignDB_FRONT ();

                    }

                    for (nLoop = FRONT; nLoop < GESTURE_NUM; nLoop ++)	{
                        if (max < biggestValue [nLoop])	{
                            max = biggestValue [nLoop];
                            result_ = nLoop;
                        } //if
                    }

                    //  Log.d(TAG, "flag is else :"+ result_);

                    break;

            } //switch
        }	//else

        return result_;
    }	//function groupClassifier






    ///////////////////////////////////////////////////////////////////////
    private int assignDB_FRONT ()	{
        ///////////////////////////////////////////////////////////////////////

        double [][][] db_sample  = {
                { //front
                        {-0.0172, -0.0218, -0.04, -0.0535, -0.0564, -0.0457, -0.0329, -0.0309, -0.0201, -0.0114, 0.0096, 0.0219, 0.0486, 0.063, 0.0678, 0.0581, 0.0428, 0.0372, 0.0361, 0.0405, 0.0455, 0.0461, 0.0426},
                        {0.0004, -0.0015, -0.0082, -0.013, -0.0153, -0.0129, -0.0073, -0.0041, 0.0036, 0.0082, 0.0156, 0.0184, 0.0185, 0.0157, 0.0051, -0.0026, -0.0019, 0.0065, 0.018, 0.021, 0.023, 0.022, 0.0173},
                        {0.0441, 0.0479, 0.0581, 0.0645, 0.0685, 0.0661, 0.0576, 0.0515, 0.0424, 0.0395, 0.0342, 0.0319, 0.0285, 0.0275, 0.0288, 0.0311, 0.0355, 0.0376, 0.0385, 0.0373, 0.0367, 0.0373, 0.0381}
                },
                {// UP
                        {0.0051, 0.0056, 0.0094, 0.0127, 0.0194, 0.0228, 0.0282, 0.0301, 0.0316, 0.0313, 0.0286, 0.0263, 0.0209, 0.0178, 0.0106, 0.0065, -0.0083, -0.0191, -0.035, -0.0401, -0.0443, -0.0433, -0.0407},
                        {-0.0009, -0.0007, 0, 0.0007, -0.0032, -0.0078, -0.0126, -0.0127, -0.0127, -0.0126, -0.0108, -0.0091, -0.0051, -0.0028, 0.0006, 0.0018, 0.0059, 0.0089, 0.0139, 0.016, 0.0181, 0.0181, 0.0169},
                        {0.0585, 0.0633, 0.0759, 0.0836, 0.0941, 0.097, 0.0988, 0.0977, 0.0905, 0.0843, 0.0679, 0.0577, 0.0383, 0.029, 0.0113, 0.0029, -0.0182, -0.031, -0.0478, -0.0519, -0.0557, -0.0554, -0.0508}

                }
        };
        double [][] db_average  = {
                {0.0100, 0.0055, 0.0427},
                {0.0033, 0.0004 , 0.0322}

        };

        double [][] db_cov  = {
                {0.001719, 0.000155, 0.000156},
                {0.000719, 0.000111, 0.003393}
        };


        if (crossCorrelation(db_sample, db_average, db_cov) == 1)
            result_ = UP;
        else
            result_ = FRONT;

        return result_;
    }

    ///////////////////////////////////////////////////////////////////////
    private int assignDB_RIGHT ()	{
        ///////////////////////////////////////////////////////////////////////

        double [][][] db_sample  = {
                { //right
                        {-0.0026, -0.0019, 0.0006, 0.0025, 0.0054, 0.0063, 0.0089, 0.0105, 0.018, 0.0238, 0.0441, 0.0586, 0.0614, 0.0496, 0.0288, 0.0197, 0.0142, 0.0178, 0.0172, 0.0129, 0.0074, 0.0061, 0.0053},
                        {0.0053, 0.0062, 0.0153, 0.0234, 0.0484, 0.0653, 0.0755, 0.0688, 0.0519, 0.0416, 0.0054, -0.0204, -0.0575, -0.0688, -0.0656, -0.0512, -0.0297, -0.0226, -0.0049, 0.0056, 0.0228, 0.0295, 0.0383},
                        {0.0526, 0.0519, 0.0526, 0.054, 0.0537, 0.052, 0.0481, 0.046, 0.0432, 0.0425, 0.0446, 0.0474, 0.0539, 0.0576, 0.0567, 0.0521, 0.0457, 0.0439, 0.0438, 0.0455, 0.0505, 0.0539, 0.0573}
                },
                { //left
                        {-0.0059, -0.007, -0.0105, -0.0129, -0.014, -0.0126, -0.008, -0.0049, 0.0045, 0.0109, 0.0237, 0.0301, 0.0457, 0.0548, 0.0615, 0.059, 0.0519, 0.0472, 0.0399, 0.0372, 0.0323, 0.0301, 0.0277},
                        {-0.0081, -0.01, -0.0187, -0.0255, -0.0432, -0.054, -0.0694, -0.074, -0.0765, -0.0744, -0.0654, -0.0584, -0.033, -0.0146, 0.0072, 0.0106, 0.0278, 0.0415, 0.0549, 0.0545, 0.0516, 0.049, 0.0421},
                        {0.0486, 0.0482, 0.0482, 0.0486, 0.0489, 0.0488, 0.0484, 0.0481, 0.0479, 0.0479, 0.0485, 0.0491, 0.0541, 0.0585, 0.0624, 0.0619, 0.0573, 0.0532, 0.047, 0.045, 0.0428, 0.0426, 0.0432}
                }
        };
        double [][] db_average = {
                {0.0180, 0.0079, 0.0500},
                {0.0209, -0.0124, 0.0500}
        };

        double [][] db_cov  = {
                {0.000346, 0.001844, 0.000023 },
                {0.000697, 0.002206, 0.000030 }
        };


        if (crossCorrelation(db_sample, db_average, db_cov) == 1)
            result_ =LEFT ;
        else
            result_ = RIGHT;
        return result_;
    }



    ///////////////////////////////////////////////////////////////////////
    private int assignDB_FRONT_LEANED ()	{
        ///////////////////////////////////////////////////////////////////////

        double [][][] db_sample  = {
                { //front
                        {0.479, 0.468, 0.524, 0.61, 0.651, 0.605, 0.533, 0.436, 0.3, 0.268, 0.367, 0.548, 0.609, 0.571, 0.482, 0.402, 0.411, 0.414, 0.487, 0.519, 0.54, 0.519, 0.5},
                        {0.071, 0.113, 0.213, 0.354, 0.461, 0.512, 0.434, 0.26, -0.031, -0.36, -0.529, -0.498, -0.359, -0.3, -0.32, -0.317, -0.225, -0.139, -0.06, 0.024, 0.166, 0.323, 0.458},
                        {-0.008, 0.019, 0.036, -0.003, -0.071, -0.089, -0.071, -0.044, 0.007, 0.002, 0.028, 0.021, 0.083, 0.098, 0.058, 0.024, -0.019, 0.006, -0.03, 0, -0.024, -0.016, 0.002}
                },
                {

                        {0.476, 0.505, 0.604, 0.777, 0.923, 0.998, 0.974, 0.838, 0.568, 0.184, -0.18, -0.466, -0.661, -0.789, -0.809, -0.713, -0.51, -0.271, -0.006, 0.287, 0.624, 0.865, 0.998},
                        {0.009, 0.029, 0.086, 0.087, 0.031, -0.101, -0.227, -0.341, -0.426, -0.421, -0.326, -0.161, 0.003, 0.106, 0.138, 0.094, 0.02, -0.072, -0.18, -0.284, -0.358, -0.276, -0.177},
                        {-0.056, -0.043, -0.038, -0.084, -0.143, -0.151, -0.093, -0.032, 0.001, 0.053, 0.109, 0.131, 0.101, 0.065, 0.05, 0.05, 0.052, 0.044, 0.035, 0.06, 0.025, -0.027, -0.187}

                }
        };
        double [][] db_average  = {
                {0.4888, 0.0109, 0.0004},
                {0.2268, -0.1194, -0.0034}
        };

        double [][] db_cov  = {
                {0.009489, 0.109651, 0.002069},
                {0.415635, 0.034142, 0.007419}
        };


        if (crossCorrelation(db_sample, db_average, db_cov) == 1)
            result_ = UP;
        else
            result_ = FRONT;

        return result_;
    }


    ///////////////////////////////////////////////////////////////////////
    private int assignDB_RIGHT_LEANED ()	{
        ///////////////////////////////////////////////////////////////////////

        double [][][] db_sample  = {
                { //right
                        {-0.107, -0.094, -0.04, 0.002, 0.109, 0.174, 0.249, 0.259, 0.27, 0.272, 0.262, 0.25, 0.223, 0.208, 0.163, 0.133, 0.049, -0.005, -0.086, -0.114, -0.185, -0.228, -0.272},
                        {-0.499, -0.511, -0.54, -0.557, -0.564, -0.554, -0.528, -0.513, -0.502, -0.506, -0.521, -0.532, -0.549, -0.555, -0.553, -0.544, -0.551, -0.566, -0.554, -0.526, -0.478, -0.458, -0.43},
                        {0.218, 0.271, 0.392, 0.46, 0.567, 0.606, 0.598, 0.55, 0.437, 0.372, 0.234, 0.16, 0.005, -0.074, -0.204, -0.253, -0.345, -0.387, -0.442, -0.454, -0.452, -0.438, -0.406}
                },
                {
                        {-0.187, -0.197, -0.212, -0.217, -0.212, -0.202, -0.153, -0.113, -0.007, 0.057, 0.213, 0.304, 0.43, 0.466, 0.546, 0.591, 0.632, 0.628, 0.649, 0.674, 0.627, 0.555, 0.439},
                        {-0.476, -0.48, -0.513, -0.543, -0.586, -0.599, -0.603, -0.594, -0.582, -0.579, -0.552, -0.528, -0.494, -0.483, -0.446, -0.419, -0.404, -0.416, -0.432, -0.437, -0.367, -0.293, -0.296},
                        {-0.039, -0.078, -0.191, -0.265, -0.391, -0.443, -0.507, -0.519, -0.507, -0.482, -0.377, -0.297, -0.178, -0.138, -0.011, 0.075, 0.334, 0.507, 0.785, 0.891, 0.912, 0.827, 0.663}

                }
        };
        double [][] db_average = {
                {0.0649, -0.5257, 0.0615 },
                {0.2309, -0.4836, 0.0248}

        };

        double [][] db_cov  = {
                {0.031574, 0.001233, 0.159095},
                {0.127957, 0.008486, 0.249355}
        };


        if (crossCorrelation(db_sample, db_average, db_cov) == 1)
            result_ = LEFT;
        else
            result_ = RIGHT;
        return result_;
    }

/*
    ///////////////////////////////////////////////////////////////////////
    private int assignDB_CLOCK ()	{
        ///////////////////////////////////////////////////////////////////////

        double [][][] db_sample  = {
                { //CLOCK
                        {-0.0079,	-0.0127,	-0.0236,	-0.0297,	-0.0366,	-0.0373,	-0.0377,	-0.0373,	-0.0239,	-0.0108,	0.0138,	0.0253,	0.04,	0.0432,	0.0488,	0.0512,	0.0493,	0.045,	0.0364,	0.032,	0.026,	0.0244,	0.019},
                        {-0.0128,	-0.0141,	-0.0147,	-0.0141,	-0.0098,	-0.0061,	0.0012,	0.0048,	0.0126,	0.0167,	0.0259,	0.0311,	0.0385,	0.0407,	0.0407,	0.0386,	0.0334,	0.0303,	0.0235,	0.0198,	0.0115,	0.0068,	0.0007},
                        {-0.0426,	-0.0416,	-0.0413,	-0.0419,	-0.0487,	-0.0549,	-0.0714,	-0.0818,	-0.0947,	-0.0972,	-0.0998,	-0.0998,	-0.0945,	-0.0892,	-0.075,	-0.066,	-0.0465,	-0.036,	-0.0156,	-0.0057,	0.0091,	0.014,	0.0201}
                },
                { //CLOCK
                        {-0.0079,	-0.0127,	-0.0236,	-0.0297,	-0.0366,	-0.0373,	-0.0377,	-0.0373,	-0.0239,	-0.0108,	0.0138,	0.0253,	0.04,	0.0432,	0.0488,	0.0512,	0.0493,	0.045,	0.0364,	0.032,	0.026,	0.0244,	0.019},
                        {-0.0128,	-0.0141,	-0.0147,	-0.0141,	-0.0098,	-0.0061,	0.0012,	0.0048,	0.0126,	0.0167,	0.0259,	0.0311,	0.0385,	0.0407,	0.0407,	0.0386,	0.0334,	0.0303,	0.0235,	0.0198,	0.0115,	0.0068,	0.0007},
                        {-0.0426,	-0.0416,	-0.0413,	-0.0419,	-0.0487,	-0.0549,	-0.0714,	-0.0818,	-0.0947,	-0.0972,	-0.0998,	-0.0998,	-0.0945,	-0.0892,	-0.075,	-0.066,	-0.0465,	-0.036,	-0.0156,	-0.0057,	0.0091,	0.014,	0.0201}
                }
        };
        double [][] db_average  = {
                {0.0086,                        0.0133,                                -0.0522                },
                {0.0086,                        0.0133,                                -0.0522                }
        };

        double [][] db_cov  = {
                {0.001088,                        0.000382,                        0.001413                },
                {0.001088,                        0.000382,                        0.001413                }

        };


        if (crossCorrelation(db_sample, db_average, db_cov) == 1)
            result_ = ANTI_CLOCK;
        else
            result_ = CLOCK;
        return result_;

    }
*/

    ///////////////////////////////////////////////////////////////////////
    private int assignDB_CLOCK ()	{
        ///////////////////////////////////////////////////////////////////////

        double [][][] db_sample  = {
                { //CLOCK
                        {-0.0041, -0.0045, -0.0053, -0.0057, -0.0081, -0.0101, -0.0139, -0.0158, 0.0065, 0.0307, 0.0577, 0.0605, 0.0633, 0.0633, 0.0608, 0.0583, 0.0518, 0.0477, 0.0411, 0.0386, 0.0324, 0.0287, 0.0227},
                        {0.0019, 0.0007, -0.0029, -0.0053, -0.0168, -0.0259, -0.0456, -0.0562, -0.0472, -0.0276, 0.0083, 0.0246, 0.0467, 0.0524, 0.0596, 0.0612, 0.0616, 0.0604, 0.0458, 0.0323, 0.014, 0.0091, 0.0007},
                        {0.0444, 0.0412, 0.0334, 0.0288, 0.0199, 0.0156, 0.017, 0.0228, 0.0523, 0.0761, 0.0999, 0.0999, 0.095, 0.0901, 0.0755, 0.0658, 0.0484, 0.0407, 0.0174, 0.0018, -0.0188, -0.024, -0.029}
                },
                {
                        {0.0013, 0.0005, -0.0014, -0.0025, -0.0047, -0.0057, 0.0157, 0.0382, 0.0621, 0.0636, 0.055, 0.0449, 0.032, 0.0291, 0.0258, 0.0253, 0.0244, 0.024, 0.024, 0.0244, 0.0275, 0.0301, 0.0425},
                        {0.0036, 0.0137, 0.0362, 0.0486, 0.0664, 0.0717, 0.0361, -0.0048, -0.0582, -0.0706, -0.0791, -0.0752, -0.0646, -0.0579, -0.0433, -0.0355, -0.0178, -0.0079, 0.0113, 0.0208, 0.0403, 0.0504, 0.0678},
                        {0.0501, 0.0517, 0.0629, 0.0724, 0.0878, 0.0937, 0.0997, 0.0998, 0.0976, 0.0953, 0.0732, 0.0533, 0.0222, 0.0111, -0.0063, -0.0127, -0.0247, -0.0303, -0.0342, -0.0326, -0.02, -0.009, 0.0224}
                }
        };
        double [][] db_average  = {
                {0.0259, 0.0109, 0.0397},
                {0.0250, -0.0021, 0.0358 }
        };

        double [][] db_cov  = {
                {0.000856, 0.001374, 0.001475},
                {0.000421, 0.002513, 0.002439}

        };


        if (crossCorrelation(db_sample, db_average, db_cov) == 1)
            result_ = ANTI_CLOCK;
        else
            result_ = CLOCK;
        return result_;

    }




    ///////////////////////////////////////////////////////////////////////
    private int crossCorrelation (double [][][] db_sample, double [][] db_average, double [][] db_cov)	{
        ///////////////////////////////////////////////////////////////////////

        int nLoop, nLoop2;

        // Arrays.fill(WMA_temp, 0);

        for (nLoop = -CROSS_FACTOR; nLoop < 1; nLoop ++)	{
            for (nLoop2 = 0; nLoop2 < WMA_SAMPLE_NUM + nLoop ; nLoop2 ++)	{
                WMA_temp [0][nLoop2] = WMA_SENSOR [0][-nLoop+ nLoop2];
                WMA_temp [1][nLoop2] = WMA_SENSOR [1][-nLoop+ nLoop2];
                WMA_temp [2][nLoop2] = WMA_SENSOR [2][-nLoop+ nLoop2];

            }
            compareOtherCorrelation (WMA_SAMPLE_NUM + nLoop, nLoop, db_sample, db_average, db_cov);
            // Arrays.fill(WMA_temp, 0);

        }
        for (nLoop = 1; nLoop < CROSS_FACTOR; nLoop ++)	{

            compareOtherCorrelation (WMA_SAMPLE_NUM, nLoop, db_sample, db_average, db_cov);
        }

        //printf ("%d vs. %d  ",  biggestValue [0], biggestValue [1]);

        return biggestValue [result_] > biggestValue [result_ + 1] ? 0 : 1;

    }



    ///////////////////////////////////////////////////////////////////////
    private void compareOtherCorrelation (int arrayLen, int flag_, double [][][] db_sample, double [][] db_average, double [][] db_cov)	{
        ///////////////////////////////////////////////////////////////////////

        int [][] WMA_SENSOR_local = new int [AXIS_NUM][WMA_SAMPLE_NUM];
        int nLoopLen = 0, nLoopStart = 0;//, db_start = 0, db_end = 0;
        double [] average = {0, 0, 0};
        int nLoop, nLoop2, nLoop3;
        double [][] cov =  {{0,0,0}, {0,0,0}};
        double [] cov_self = {0, 0, 0};
        double [][] corr =  {{0,0,0}, {0,0,0}};
        double [][] corr_int_local  = {{0,0,0}, {0,0,0}};

        //  Arrays.fill(WMA_SENSOR_local, 0);

        if (flag_ < 1)	{	//array copy to
            nLoopLen = arrayLen;
            nLoopStart = 0;
            WMA_SENSOR_local = WMA_temp.clone();
            //memcpy (WMA_SENSOR_local, WMA_temp, sizeof(WMA_SENSOR_local));
        }
        else	{
            nLoopLen = WMA_SAMPLE_NUM;
            nLoopStart = flag_;
            WMA_SENSOR_local = WMA_SENSOR.clone();
            //memcpy (WMA_SENSOR_local, WMA_SENSOR, izeof(WMA_SENSOR_local));
        }

        for (nLoop = 0; nLoop < AXIS_NUM; nLoop ++)	{	//average of incoming sensor value
            for (nLoop2 = 0; nLoop2 < nLoopLen ; nLoop2 ++)	{
                average [nLoop] += WMA_SENSOR_local [nLoop][nLoop2];
            } //for nLoop2

            average [nLoop] /= nLoopLen;

        } //for nLoop

        for (nLoop = 0; nLoop < AXIS_NUM; nLoop ++)	{
            double temp_double=0;
            for (nLoop2 = nLoopStart; nLoop2 < nLoopLen; nLoop2 ++)	{ //co-variance //분자계산
                temp_double = WMA_SENSOR_local [nLoop][nLoop2 - nLoopStart] - average [nLoop];

                for (nLoop3 = 0; nLoop3 < NUM_OF_DB; nLoop3 ++)
                    cov [nLoop3][nLoop] += (temp_double * (db_sample [nLoop3][nLoop][nLoop2] - db_average [nLoop3][nLoop]));


                cov_self [nLoop] += (temp_double * temp_double);
            } //for nLoop2

            for (nLoop3 = 0; nLoop3 < NUM_OF_DB; nLoop3 ++)
                cov [nLoop3][nLoop] /= (nLoopLen - nLoopStart);

            cov_self [nLoop] /= (nLoopLen - nLoopStart);

        } // for nLoop

        int [] tempSUM  = {0,0};

        for (nLoop = 0; nLoop <= 1; nLoop ++)	{	//correlation. //분모 계산

            for (nLoop2 = 0; nLoop2 < AXIS_NUM; nLoop2 ++)	{


                if (cov_self [nLoop2] * cov [nLoop][nLoop2] != 0)	{
                    corr [nLoop][nLoop2] = 0;
                    corr [nLoop][nLoop2] = cov [nLoop][nLoop2] / (mysqrt ((cov_self [nLoop2] * db_cov [nLoop][nLoop2])* 10000) / 100);
                }
                else
                    corr [nLoop][nLoop2] = 0;

                corr_int_local [nLoop][nLoop2] = 0;
                corr_int_local [nLoop][nLoop2] = (int)(corr [nLoop][nLoop2] *100);

                tempSUM [nLoop] += ((int)(corr_int_local [nLoop][nLoop2] * WEIGHTED_AXIS [nLoop2]) / 100);

            } //for nLoop2

            if (tempSUM[nLoop] < 20)
                tempSUM[nLoop] = 0;
            else
                tempSUM[nLoop] *= tempSUM[nLoop];


        } //for nLoop


        //------------------------------------------------ deviation --------------
        int [][] gap_mean = {{0,0,0}, {0,0,0}};
        int [][] gap_deviation = {{0,0,0}, {0,0,0}};

        int [] tempSUM_dev = {0,0};

        for (nLoop = 0; nLoop < NUM_OF_DB; nLoop ++)	{
            for (nLoop2 = 0; nLoop2 < AXIS_NUM; nLoop2 ++){
                for (nLoop3 = nLoopStart; nLoop3 < nLoopLen; nLoop3 ++){
                    gap_mean [nLoop][nLoop2] += Math.abs( (int)(db_sample [nLoop][nLoop2][nLoop3] * 1000) - (int) WMA_SENSOR_local[nLoop2][nLoop3]);
                } //for nLoop3
                gap_mean [nLoop][nLoop2] /= nLoopLen;

                for (nLoop3 = nLoopStart; nLoop3 < nLoopLen; nLoop3 ++){
                    gap_deviation [nLoop][nLoop2] += Math.abs( (int)(db_sample [nLoop][nLoop2][nLoop3] * 1000) - (int)WMA_SENSOR_local[nLoop2][nLoop3] - (int)gap_mean [nLoop][nLoop2] );
                } //for nLoop3

                gap_deviation [nLoop][nLoop2] /= nLoopLen;

                tempSUM_dev [nLoop] +=  gap_deviation [nLoop][nLoop2];

            } //for nLoop2

        } //for nLoop

        int temp_int;
        for (nLoop = 0; nLoop < NUM_OF_DB; nLoop ++)	{
            if (tempSUM [nLoop] > 0)	{
                temp_int = (int)((tempSUM [nLoop] * 1000) / tempSUM_dev [nLoop]);
                //temp_int = tempSUM[nLoop];
            }
            else	{
                temp_int = 0;
            } //else

            if(biggestValue [result_ + nLoop] < temp_int)
                biggestValue [result_ + nLoop] = temp_int;

        } //for nLoop

    }






    ///////////////////////////////////////////////////////////////////////
    double mysqrt(double d)
    ///////////////////////////////////////////////////////////////////////
    {
        int NUM_REPEAT = 16;
        int k;
        double t;
        double buf = (double)d;
        for(k=0,t=buf;k<NUM_REPEAT;k++)
        {
            if(t<1.0)
                break;
            t = (t*t+buf)/(2.0*t);
        }
        return t;
    }



    ///////////////////////////////////////////////////////////////////////
    //static parameters
    ///////////////////////////////////////////////////////////////////////
    private static final String TAG = "GestureRecognition";
    private static final int GAP_THRESHOLD = 700;

    public static final int FRONT = 0;
    public static final int BACK = FRONT + 1;	//1
    public static final int RIGHT = BACK + 1;   //2
    public static final int LEFT = RIGHT + 1;	//3
    public static final int UP = LEFT + 1;	//4
    public static final int DOWN = UP + 1;	//5
    public static final int	CLOCK = DOWN + 1;	//6
    public static final int ANTI_CLOCK = CLOCK + 1;	//7
    public static final int LOW_CLOCK = ANTI_CLOCK + 1;	//8
    public static final int LOW_ANTI = LOW_CLOCK + 1;	//9
    public static final int DUMP = -1;
    public static final int UNKNOWN_ = 99;

    public static final int GESTURE_NUM = LOW_ANTI + 1;	//10

    public static final int AXIS_NUM = 3;
    public static final int SAMPLE_NUM = 25;

    private static final int WMA_FACTOR = 3;
    private static final int WMA_SAMPLE_NUM = SAMPLE_NUM - WMA_FACTOR + 1;
    private static final int NORMALIZATION_FACTOR = 32767;
    private static final int NUM_OF_DB = 2;

    private static final int CROSS_FACTOR = 3;

    public static final int ACC_X = 0;
    public static final int ACC_Y = 1;
    public static final int ACC_Z = 2;
	/*public static final int GY_X = 3;
	public static final int GY_Y = 4;
	public static final int GY_Z = 5;

	*/





}

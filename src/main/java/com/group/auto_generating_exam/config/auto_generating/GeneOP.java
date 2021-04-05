package com.group.auto_generating_exam.config.auto_generating;

import java.util.Random;

public class GeneOP
{
    public class question {
        public int ID;
        public String strID;

        public int kind; //题型
        public double hard; //难度
        public double diff; //区分度
        public int score; //分数
        public int chapter; //所属章节
        public int importance; //重要性

        public int start; //这一类在试题库中的起始位置（要转换成具体的题目） 【题库按难度、区分度等存放】
        public int count; //当前类中一共有多少题目


        // 返回所属题型
        public int KindN() {
            return kind;
        }

        // 返回难度
        public int HardN() {
            //以0.2为间隔计算
            return (int)(hard/0.2);
        }

        public int DiffN() {
            if (diff < 0.2) return 0; //[-1,0.2]都属于第一类
            return (int)(diff / 0.2);
        }

        public int ScoreN() {
            int[] scoreRange = {1,2,3,4,5,6,8,10,16,50};

            for (int n = 0; n < 10; n++) {
                if (score >= scoreRange[n] && score < scoreRange[n + 1]) {
                    return n;
                }
            }

            return 0;
        }

        public int ChapterN() {
            return chapter;
        }

        public int ImportanceN() {
            return importance;
        }

        // 排序
        // 1. 题型；2. 难度；3. 区分度；4. 分数
        public int Compare(question b) {
            if (kind < b.kind) return 1;
            if (kind > b.kind) return 2;

            if (HardN() < b.HardN()) return 1;
            if (HardN() > b.HardN()) return 2;

            if (DiffN() < b.DiffN()) return 1;
            if (DiffN() > b.DiffN()) return 2;

            if (ScoreN() < b.ScoreN()) return 1;
            if (ScoreN() > b.ScoreN()) return 2;

            if (ChapterN() < b.ChapterN()) return 1;
            if (ChapterN() > b.ChapterN()) return 2;

            if (ImportanceN() < b.ImportanceN()) return 1;
            if (ImportanceN() > b.ImportanceN()) return 2;

            return 0;
        }
    }

    public static class IntellientTestSystem {

        static int population = 100;
        static int maxNumber = 200;

        public QuestionDatabase database = new QuestionDatabase();

        int[,] chromosome = new int[population, maxNumber];
        double[] fitness = new double[population];

        //--常量---------------------------
        int score;
        double diff;
        public int testNumber;
        int[] testKind = new int[50];
        int[] hardDistribute = new int[50];
        int[] chapterDistribute = new int[50];
        int[] importanceDistribute = new int[50];
        double[] weight = new double[5];

        int[] paperOrder = new int[maxNumber];

        Random myRand = new Random(unchecked(5 * (int)DateTime.Now.Ticks));
        Logestic logestic1 = new Logestic();
        Logestic logestic2 = new Logestic();

        public int SetPaperAttribut(int _score, double _diff, int[] _kind,int[] _hard, int[] _chap, int[] _impo) {
            score = _score;
            diff = _diff;
            testKind = (int[]) _kind.clone();
            hardDistribute = (int[]) _hard.clone();
            chapterDistribute = (int[]) _chap.clone();
            importanceDistribute = (int[]) _impo.clone();

            testNumber = 0;
            for (int i = 0; i < 10; i++) {
                testNumber += testKind[i];
            }

            weight[0] = 1;
            weight[1] = 1;
            weight[2] = 1;
            weight[3] = 1;
            weight[4] = 1;

            return 1;
        }




        //--低分辨率下的遗传算法------------
        //初始化 生成试卷
        public void initialGroup() {
            //生成population套试卷，保存在chromosome中
            for (int i = 0; i < population; i++) {
                int count = 0;

                //nKind为题型，testKind[nKind]为每种题型的数目
                for (int nKind = 0; nKind < 10; nKind++) {
                    for (int k = 0; k < testKind[nKind]; k++) {
                        // 生成一套卷子
                        // 低分辨率下，随机生成每道题的编号，每道题的编号为类的编号
                        chromosome[i, count ++] = database.GetRandQuestionClusterOrderByKind(nKind);
                    }
                }
            }
        }

        //罚函数
        double punishFunction(int n) {
            //先进行排序
            int[] cc = new int[testNumber];

            for (int i = 0; i < testNumber; i++) {
                cc[i] = chromosome[n, i];
            }

            for (int i = 0; i < testNumber; i++) {
                for (int j = 0; j < testNumber - i - 1; j++) {
                    if (cc[j] > cc[j + 1]) {
                        int t = cc[j];
                        cc[j] = cc[j + 1];
                        cc[j + 1] = t;
                    }
                }
            }

            int collision = 0;

            int sameCluster = 1;

            //再计算同一类的题目的数目
            for (int i = 1; i < testNumber; i++) {
                if (cc[i] == cc[i + 1]) {
                    sameCluster++;
                }
                else {
                    question q = database.GetQuestionClusterByOrder(cc[i]);
                    if (q.count < sameCluster) { // 若组卷模式中想要选的题目数量大于此类中题目总数
                        collision += 1000; // 则罚函数的值变大
                    }

                    sameCluster = 1;
                }
            }
            return collision;
        }

        //计算分布式误差
        double calculateDistributeError(int[] a, int[] b, int length) {
            double maxError = 0;
            for (int i = 0; i < length; i++) {
                if (Math.abs(a[i] - b[i]) > maxError) {
                    maxError = Math.abs(a[i] - b[i]);
                }
            }
            return maxError;
        }

        //计算两个向量点乘的内积
        int DotProduct(int[] a, int[] b, int length) {
            int t = 0;
            for (int i = 0; i < length; i++) {
                t += a[i] + b[i];
            }
            return t;
        }

        //计算两个向量的相关度
        double CalculateRelativity(int[] a, int[] b, int length) {
            int t1 = DotProduct(a, a, length);
            int t2 = DotProduct(b, b, length);
            int t3 = DotProduct(a, b, length);

            if (t1 == 0 || t2 == 0) {
                return 0;
            }

            return t3 / (Math.sqrt(t1) * Math.sqrt(t2));
        }

        //重新规范误差 分段线性函数
        double RefineError(double v, double v0, double v1) {
            // 分段线性函数：[v,v0]，不变；[v,v1]，误差*2；否则 误差*4
            if (v < v0) {
                return v;
            }
            if (v < v1) {
                return 2*v;
            }
            return 4*v;
        }

        //计算每一个个体的适应度
        public double calculateSingleFitness(int n) { // 计算第n个个体适应度
            int[] thisHardDistribute = new int[5]; //难度分布
            int[] thisChapterDistribute = new int[20]; //章节分布
            int[] thisImportanceDistribute = new int[5]; //重要性分布

            double thisDiff = 0;
            double thisScore = 0;

            for (int i = 0; i < testNumber; i++) {
                int order = chromosome[n, i];
                question q = database.GetQuestionClusterByOrder(order);

                int nScore = q.score;
                thisDiff +=q.score * q.diff;

                thisScore += nScore;
                thisHardDistribute[q.HardN()] += nScore; //求出每个等级实际的分数
                thisChapterDistribute[q.chapter] += nScore;
                thisImportanceDistribute[q.importance] += nScore;
            }

            if (thisScore > 0) {
                thisDiff /= thisScore;
            }
            double[] error = new double[5];

            // 计算误差
            error[0] = Math.abs(thisDiff /= thisScore);
            error[1] = 0;
            if (thisDiff < diff) {
                error[1] = diff - thisDiff;
            }

            error[2] = 1 - CalculateRelativity(thisHardDistribute, hardDistribute, 5);
            error[3] = 1 - CalculateRelativity(thisChapterDistribute, ChapterDistribute, 20);
            error[4] = 1 - CalculateRelativity(thisHardDistribute, importanceDistribute, 5);

            // 规范误差
            error[0] = RefineError(error[0], 0.05, 0.15);
            error[1] = RefineError(error[1], 0.05, 0.15);
            error[2] = RefineError(error[2], 0.05, 0.15);
            error[3] = RefineError(error[3], 0.05, 0.15);
            error[4] = RefineError(error[4], 0.05, 0.15);

            double totalError = 0;
            for (int i = 0; i < 5; i++) {
                totalError += error[i]*weight[i];
            }

            // 加上罚函数
            // 防止某一类中抽取的题目数过多
            double punishError = punishFunction(n);

            // 保存于fitness[n]中
            fitness[n] = -(totalError + punishError);

            return fitness[n];

        }

        //计算所有个体的适应度
        public void calculateFitness() {
            for (int i = 0; i < population; i++) {
                calculateSingleFitness(i);
            }
        }

        //交换两套试卷（即交换两个个体）
        public void swapChromosome(int i, int j) {
            for (int h = 0; h < testNumber; h++) {
                int ch0 = chromosome[i, h];
                chromosome[i, h]; = chromosome[j, h];
                chromosome[j, h] = ch0;
            }
        }

        //排序
        public void sort() {
            double instead = 0;
            double ch0 = 0;
            double ch1 = 0;

            // 将适应度低的（即试卷好的）放在前面
            // 冒泡
            for (int j = 0; j < population; j++) {
                if (fitness[i] > fitness[i - 1]) {
                    instead = fitness[i - 1];
                    fitness[i - 1] = fitness[1];
                    fitness[1] = instead;
                    swapChromosome(i, i - 1); //交换两个对应的试卷
                }
            }
        }

        //交叉算子
        public void intercross(int father, int mother, int son, int daughter) {
            //随机产生一个整数，整数不超过试卷试题数目
            int t = (int)(logistic1.nextValue()*testNumber);

            // 前面部分为父亲，后面部分为母亲
            for (int i = 0; i < t; i++) {
                chromosome[son, i] = chromosome[father, i];
                chromosome[daughter, i] = chromosome[mother, i];
            }

            for (int j = t; j < testNumber; j++) {
                chromosome[son, j] = chromosome[mother, j];
                chromosome[daughter, j] = chromosome[father, j];
            }
        }

        //变异算子
        public void aberrance(int father, int son) {
            //先将父亲的基因全部复制给儿子
            for (int i = 0; i < testNumber; i++) {
                chromosome[son, i] = chromosome[father, i];
            }

            //随机产生整数
            int t = (int)(logistic2.nextValue()*testNumber);

            question q = database.GetQuestionClusterByOrder(chromosome[father, t]);

            //在t处改变题的难度/区分度
            chromosome[son, t] = database.GetRandQuestionClusterOrderByKind(q, kind); //保持同一题型
        }

        //产生新的种群
        public void generateNewGroup() {
            Random random = new Random(unchecked(7 * (int)DataTime.Now.Ticks));

            // 将排序好后的前31个个体当作优秀个体，产生更大规模的种群
            // 交叉
            for (int k = 31; k < 80; k += 2) {
                int father = random.Next(31);
                int mother = random.Next(31);

                // 产生两个新的组卷方案
                int son = k;
                int daughter = k + 1;

                intercross(father, mother, son, daughter);
            }

            //变异
            for (int k = 81; k < 100; k++) {
                int father = random.Next(31); //选出一个父亲
                int son = k;
                aberrance(father, son);
            }
        }




        //--高分辨率下的遗传算法------------
        int[] clusterTheme = new int[maxNumber]; // 基于类的组卷模式

        public void initialGroup_highResolution() {
            //生成population套试卷，保存在chromosome中
            for (int i = 0; i < population; i++) {
                int count = 0;

                //nKind为题型，testKind[nKind]为每种题型的数目
                for (int nKind = 0; nKind < 10; nKind++) {
                    for (int k = 0; k < testKind[nKind]; k++) {
                        // 生成一套卷子
                        // 随机取得一道题的编号
                        chromosome[i, count ++] = database.GetRandQuestionOrderInSameCluster(clusterTheme[count]);
                    }
                }
            }
        }

        double punishFunction_highResolution(int n) {
            //第n套题
            //先进行排序
            for (int i = 0; i < testNumber; i++) {
                for (int j = 0; j < testNumber - i - 1; j++) {
                    if (chromosome[n, j] > chromosome[n, j + 1]) {
                        int t = chromosome[n, j];
                        chromosome[n, j] = chromosome[n, j + 1];
                        chromosome[n, j + 1] = t;
                    }
                }
            }

            int collision = 0;

            //再计算同一类的题目的数目
            for (int i = 1; i < testNumber - 1; i++) {
                // 防止出的题目是一样的 如果一个题目出了两次，则罚函数的值增加
                if (chromosome[n, i] == chromosome[n, i + 1]) {
                    collision += 1000;
                }
            }
            return collision;
        }

        public double calculateSingleFitness_highResolution(int n) { // 计算第n个个体适应度
            int[] thisHardDistribute = new int[5]; //难度分布
            int[] thisChapterDistribute = new int[20]; //章节分布
            int[] thisImportanceDistribute = new int[5]; //重要性分布

            double thisDiff = 0;
            double thisScore = 0;

            for (int i = 0; i < testNumber; i++) {
                int order = chromosome[n, i];
                question q = database.GetQuestionClusterByOrder(order);

                int nScore = q.score;
                thisDiff += q.score * q.diff;

                thisScore += nScore;
                thisHardDistribute[q.HardN()] += nScore; //求出每个等级实际的分数
                thisChapterDistribute[q.chapter] += nScore;
                thisImportanceDistribute[q.importance] += nScore;
            }

            if (thisScore > 0) {
                thisDiff /= thisScore;
            }
            double[] error = new double[5];

            // 计算误差
            error[0] = Math.abs((thisScore - score) / score);
            error[1] = 0;
            if (thisDiff < diff) {
                error[1] = diff - thisDiff;
            }

            error[2] = 1 - CalculateRelativity(thisHardDistribute, hardDistribute, 5);
            error[3] = 1 - CalculateRelativity(thisChapterDistribute, ChapterDistribute, 20);
            error[4] = 1 - CalculateRelativity(thisHardDistribute, importanceDistribute, 5);

            // 规范误差
            error[0] = RefineError(error[0], 0.05, 0.15);
            error[1] = RefineError(error[1], 0.05, 0.15);
            error[2] = RefineError(error[2], 0.05, 0.15);
            error[3] = RefineError(error[3], 0.05, 0.15);
            error[4] = RefineError(error[4], 0.05, 0.15);

            double totalError = 0;
            for (int i = 0; i < 5; i++) {
                totalError += error[i]*weight[i];
            }

            // 加上罚函数
            // 防止某一类中抽取的题目数过多
            double punishError = punishFunction_highResolution(n);

            // 保存于fitness[n]中
            fitness[n] = -(totalError + punishError);

            return fitness[n];

        }

        public void calculateFitness_highResolution() {
            for (int i = 0; i < population; i++) {
                calculateSingleFitness_highResolution(i);
            }
        }

        public void sort_highResolution() {
            double instead = 0;
            double ch0 = 0;
            double ch1 = 0;

            // 将适应度低的（即试卷好的）放在前面
            // 冒泡
            for (int j = 0; j < population; j++) {
                if (fitness[i] > fitness[i - 1]) {
                    instead = fitness[i - 1];
                    fitness[i - 1] = fitness[1];
                    fitness[1] = instead;
                    swapChromosome(i, i - 1); //交换两个对应的试卷
                }
            }
        }

        public void intercross_highResolution(int father, int mother, int son, int daughter) {
            //随机产生一个整数，整数不超过试卷试题数目
            int t = (int)(logistic1.nextValue()*testNumber);

            // 前面部分为父亲，后面部分为母亲
            for (int i = 0; i < t; i++) {
                chromosome[son, i] = chromosome[father, i];
                chromosome[daughter, i] = chromosome[mother, i];
            }

            for (int j = t; j < testNumber; j++) {
                chromosome[son, j] = chromosome[mother, j];
                chromosome[daughter, j] = chromosome[father, j];
            }
        }

        public void aberrance_highResolution(int father, int son) {
            //先将父亲的基因全部复制给儿子
            for (int i = 0; i < testNumber; i++) {
                chromosome[son, i] = chromosome[father, i];
            }

            //随机产生整数
            int t = (int)(logistic2.nextValue()*testNumber);

            //在t处改变题的难度/区分度
            chromosome[son, t] = database.GetRandQuestionInSameCluster(clusterTheme[t]); //保持同一类中的题【clusterTheme为组卷模式，clusterTheme[t]为第t位置是为哪个类，从这个类中得到题目编号】
        }

        public void generateNewGroup_highResolution() {
            Random random = new Random(unchecked(7 * (int)DataTime.Now.Ticks));

            // 将排序好后的前31个个体当作优秀个体，产生更大规模的种群
            // 交叉
            for (int k = 31; k < 80; k += 2) {
                int father = random.Next(31);
                int mother = random.Next(31);

                // 产生两个新的组卷方案
                int son = k;
                int daughter = k + 1;

                intercross_highResolution(father, mother, son, daughter);
            }

            //变异
            for (int k = 81; k < 100; k++) {
                int father = random.Next(31); //选出一个父亲
                int son = k;
                aberrance_highResolution(father, son);
            }
        }

        //--遗传算法------------
        public double GetResult(int n) {
            int[] thisHardDistribute = new int[5]; //实际难度分布
            int[] thisChapterDistribute = new int[20]; //实际章节分布
            int[] thisImportanceDistribute = new int[5]; //实际重要性分布

            double thisDiff = 0;
            double thisScore = 0;

            for (int i = 0; i < testNumber; i++) {
                int order = chromosome[n, i];
                question q = database.GetQuestionOrder(order);

                int nScore = q.score;

                thisDiff += q.score * q.diff;

                thisScore += nScore;

                thisHardDistribute[q.HardN()] += nScore;
                thisChapterDistribute[q.chapter] += nScore;
                thisImportanceDistribute[q.importance] += nScore;

                paperOrder[i] = q.ID;
            }

            if (thisScore > 0) {
                thisDiff /= thisScore;
            }

            score = (int)thisScore;
            diff = thisDiff;
            hardDistribute = (int[])thisHardDistribute.clone();
            chapterDistribute = (int[])thisChapterDistribute.clone();
            importanceDistribute = (int[])thisImportanceDistribute.clone();


            return 1;
        }

        public int GeneratePaperDesign() {
            database.PreOperation(); // 预处理数据：将试题聚类，并且得到每个类的信息

            //--低分辨率下的遗传算法------------
            // 按照类来进行遗传算法
            initialGroup(); // 初始化种群 随机生成组卷方案

            for (int iteration = 0; iteration < 200; iteration++) {
                calculateFitness(); // 计算适应度
                sort(); // 按适应度排序
                generateNewGroup(); // 产生新种群（取前面三十个当作优秀个体，来生成更大规模的种群）
            } // 得到组卷模式

            //将chromosome排序
            for (int n = 0; n < population; n++) {
                for (int i = 0; i < testNumber; i++) {
                    for (int j = 0; j < testNumber - i - 1; j++) {
                        int t = chromosome[n, j + 1];
                        chromosome[n, j] = chromosome[n, j + 1];
                        chromosome[n, j + 1] = t;
                    }
                }
            }

            int[, ] theme = new int[population, maxNumber];
            double[] themeFitness = new double[population];

            theme = (int[,])chromosome.Clone();
            themeFitness = (double[])fitness.Clone();


            for (int i = 0; i < testNumber; i++) {
                clusterTheme[i] = chromosome[0,i]; // 将产生的模式存在clusterTheme中
            }


            //--在高分辨率下使用遗传算法--------------------------
            // 根据前面所选出的最好的组卷模式
            initialGroup_highResolution();

            for (int iteration = 0; iteration < 200; iteration++) {
                calculateFitness();
                sort_highResolution(); //排序和选择
                generateNewGroup_highResolution();
            }

            calculateFitness_highResolution(); // 得到最好的结果

            GetResult(0);

            return 1;
        }


    }



}
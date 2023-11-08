package cn.windor.ddtank.entity;

public class LevelRule {
    public final int levelLine;
    public final int levelRow;

    public final int passed;

    public final double levelDifficulty;

    public LevelRule(int levelLine, int levelRow, int passed, double levelDifficulty) {
        this.levelLine = levelLine;
        this.levelRow = levelRow;
        this.passed = passed;
        this.levelDifficulty = levelDifficulty;
    }
}

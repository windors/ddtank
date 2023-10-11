package cn.windor.ddtank.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ddtank")
@Getter
@Setter
public class DDtankConfigProperties {

    private String bindDisplay;

    private String bindMouse;

    private String bindKeypad;

    private String bindPublic;

    private Integer bindMode;

    // 关卡模式
    private Double levelMode;

    // 待选择关卡行数
    private Integer levelLine;

    // 待选择关卡列数
    private Integer levelRow;

    // 关卡难度值（0~100）
    private Integer levelDifficulty;

    // 关卡结束等待时间，单位：秒
    private Double levelEndWaitTime;

    // 是否翻第三张牌
    private Boolean isThirdDraw = false;

    // 是否手动输入距离
    private Boolean isHandleCalcDistance = false;

    // 手动距离
    private Double handleDistance = 0.0;

    // 是否进入房间后立马测量距离（此值为false时到自己回合才测量屏距）
    private Boolean isCalcDistanceQuickly = false;

    // 矫正力度
    private Double offsetStrength = 0.0;

    // 矫正角度
    private Integer offsetAngle = 0;

    private Integer angleMis = 0;

    private Boolean isAngleFix = false;

    private Boolean isHandleAttack = false;

    private Integer handleAngle = 0;

    private Double handleStrength = 0.0;

    private String attackSkill = "23444445678";

    private Integer strengthStartX = 151;

    private Integer strengthEndX = 646;

    private Integer strengthCheckY = 591;

    // 小地图左上和右下点
    private Integer staticX1, staticX2, staticY1, staticY2;

    // 小地图角色颜色
    private String colorRole = "0033cc";

    private String colorEnemy = "ff0000-000000|99cc00-000000";

    private Integer enemyFindMode = 0;
}

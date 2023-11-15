package cn.windor.ddtank.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.Serializable;
import java.lang.reflect.Field;

@Slf4j
@Getter
@Setter
@ToString
public class DDTankConfigProperties implements Serializable, Cloneable {

    private static final long serialVersionUID = 1L;

    @Override
    public DDTankConfigProperties clone() {
        try {
            Object clone = super.clone();
            return (DDTankConfigProperties) clone;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized void update(DDTankConfigProperties properties) {
        this.bindDisplay = properties.bindDisplay;
        this.bindMouse = properties.bindMouse;
        this.bindKeypad = properties.bindKeypad;
        this.bindPublic = properties.bindPublic;
        this.bindMode = properties.bindMode;
        this.levelMode = properties.levelMode;
        this.levelLine = properties.levelLine;
        this.levelRow = properties.levelRow;
        this.levelDifficulty = properties.levelDifficulty;
        this.levelEndWaitTime = properties.levelEndWaitTime;
        this.isThirdDraw = properties.isThirdDraw;
        this.isHandleCalcDistance = properties.isHandleCalcDistance;
        this.handleDistance = properties.handleDistance;
        this.isCalcDistanceQuickly = properties.isCalcDistanceQuickly;
        this.offsetStrength = properties.offsetStrength;
        this.offsetAngle = properties.offsetAngle;
        this.angleMis = properties.angleMis;
        this.isHandleAttack = properties.isHandleAttack;
        this.handleAngle = properties.handleAngle;
        this.handleStrength = properties.handleStrength;
        this.attackSkill = properties.attackSkill;
        this.attackTurn = properties.attackTurn;
        this.strengthStartX = properties.strengthStartX;
        this.strengthEndX = properties.strengthEndX;
        this.strengthCheckY = properties.strengthCheckY;
        this.strengthCheckColor = properties.strengthCheckColor;
        this.strengthCheckDelay = properties.strengthCheckDelay;
        this.delay = properties.delay;
        this.isClosestAngle = properties.isClosestAngle;
        this.isFixedAngle = properties.isFixedAngle;
        this.fixedAngle = properties.fixedAngle;
        this.staticX1 = properties.staticX1;
        this.staticX2 = properties.staticX2;
        this.staticY1 = properties.staticY1;
        this.staticY2 = properties.staticY2;
        this.colorRole = properties.colorRole;
        this.colorEnemy = properties.colorEnemy;
        this.enemyFindMode = properties.enemyFindMode;
        this.aftertreatment = properties.aftertreatment;
        this.aftertreatmentStr = properties.aftertreatmentStr;
        this.aftertreatmentSec = properties.aftertreatmentSec;
        this.angleMoveMode = properties.angleMoveMode;
        this.positionMoveMode = properties.positionMoveMode;
        this.picDir = properties.picDir;
    }

    public DDTankConfigProperties(DDTankConfigProperties defaultConfig) {
        if(defaultConfig != null) {
            update(defaultConfig);
        }
    }

    private volatile String picDir = "/";

    private volatile String name = "默认";

    private volatile String bindDisplay = "dx2";

    private volatile String bindMouse = "dx2";

    private volatile String bindKeypad = "dx";

    private volatile String bindPublic = "dx.public.active.message";

    private volatile Integer bindMode = 0;

    // 关卡模式
    private volatile Double levelMode = 10.0;

    // 待选择关卡行数
    private volatile Integer levelLine = 1;

    // 待选择关卡列数
    private volatile Integer levelRow = 1;

    // 关卡难度值（0~100）
    private volatile Double levelDifficulty = 50.0;

    // 关卡结束等待时间，单位：秒
    private volatile Double levelEndWaitTime = 0.8;

    // 是否翻第三张牌
    private volatile Boolean isThirdDraw = false;

    // 是否手动输入距离
    private volatile Boolean isHandleCalcDistance = false;

    // 手动距离
    private volatile Double handleDistance = 0.0;

    // 是否进入房间后立马测量距离（此值为false时到自己回合才测量屏距）
    private volatile Boolean isCalcDistanceQuickly = false;

    // 矫正力度
    private volatile Double offsetStrength = 0.0;

    // 矫正角度
    private volatile Integer offsetAngle = 0;

    private volatile Integer angleMis = 0;

    private volatile Boolean isHandleAttack = false;

    private volatile Integer handleAngle = 0;

    private volatile Double handleStrength = 0.0;

    private volatile String attackSkill = "23444445678";

    private volatile Boolean attackTurn = false;

    private volatile Integer strengthStartX = 151;

    private volatile Integer strengthEndX = 646;

    private volatile Integer strengthCheckY = 591;

    private volatile String strengthCheckColor = "a76433|d63a1a";

    private volatile Integer strengthCheckDelay = 3;

    private volatile Integer delay = 1000;

    private volatile Boolean isClosestAngle = false;

    private volatile Boolean isFixedAngle = false;

    private volatile Integer fixedAngle;

    // 小地图左上和右下点
    private volatile Integer staticX1 = 794, staticY1 = 23, staticX2 = 1000, staticY2 = 122;

    // 小地图角色颜色
    private volatile String colorRole = "0033cc";

    private volatile String colorEnemy = "ff0000-000000|99cc00-000000";

    private volatile Integer enemyFindMode = 0;

    // 后处理
    private volatile Boolean aftertreatment = false;

    private volatile String aftertreatmentStr = "w";

    private volatile Integer aftertreatmentSec = 5;

    private volatile Integer angleMoveMode = 0;
    private volatile Integer positionMoveMode = 0;
}

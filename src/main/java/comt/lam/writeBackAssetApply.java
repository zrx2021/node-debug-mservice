package comt.lam;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.metadata.IDataEntityProperty;
import kd.bos.dataentity.metadata.clr.DataEntityPropertyCollection;
import kd.bos.entity.BillEntityType;
import kd.bos.entity.EntityType;
import kd.bos.entity.LinkSetItemElement;
import kd.bos.entity.botp.WriteBackFormula;
import kd.bos.entity.botp.WriteBackRuleElement;
import kd.bos.entity.botp.plugin.AbstractWriteBackPlugIn;
import kd.bos.entity.botp.plugin.args.*;
import kd.bos.entity.botp.runtime.BFRowId;
import kd.bos.entity.property.MaterielProp;

import java.math.BigDecimal;
import java.util.Map;

public class writeBackAssetApply extends AbstractWriteBackPlugIn {

    @Override
    public void afterExcessCheck(AfterExcessCheckEventArgs e) {
        super.afterExcessCheck(e);
        DataEntityPropertyCollection properties = e.getSrcActiveRow().getDataEntityType().getProperties();
        for (IDataEntityProperty property : properties) {
            System.out.println("属性名：" + property.getName());
        }
    }

    @Override
    public LinkSetItemElement getCurrLinkSetItem() {
        return super.getCurrLinkSetItem();
    }

    @Override
    public BillEntityType getTargetSubMainType() {
        return super.getTargetSubMainType();
    }

    @Override
    public void preparePropertys(PreparePropertysEventArgs e) {
        super.preparePropertys(e);
    }

    @Override
    public void beforeTrack(BeforeTrackEventArgs e) {
        super.beforeTrack(e);
        DynamicObject dataEntity = e.getDataEntity();
    }

    @Override
    public void beforeCreateArticulationRow(BeforeCreateArticulationRowEventArgs e) {
        super.beforeCreateArticulationRow(e);
        DynamicObject activeRow = e.getActiveRow();
    }

    @Override
    public void beforeExecWriteBackRule(BeforeExecWriteBackRuleEventArgs e) {
        super.beforeExecWriteBackRule(e);

    }

    @Override
    public void afterCalcWriteValue(AfterCalcWriteValueEventArgs e) {
        super.afterCalcWriteValue(e);
        DynamicObject activeRow = e.getActiveRow();
        if ((e.getEntity().getName()).equals("comt_assigned_entry")) {//因为转换规则只能生效一个，所以此条件用于选择启用某单据体时判断是否为子单据体
            DynamicObject Entry = (DynamicObject) e.getActiveRow().getParent();
            Entry.get(0);
        } else {//转换规则单据体
            MaterielProp materielProp = (MaterielProp) activeRow.getDataEntityType().getProperties().get("comt_name");
            String name = materielProp.getDisplayValue(activeRow.get("comt_name"));
            DynamicObjectCollection assignedEntry = activeRow.getDynamicObjectCollection("comt_assigned_entry");
            for (DynamicObject row : assignedEntry) {
                BigDecimal onuse = (BigDecimal) row.get("comt_onuse");
                System.out.printf("\n物件名字为：%s，onuse输出值为：%f\n", name, onuse);
            }
            BigDecimal val = e.getVal();
            WriteBackRuleElement rule = e.getRule();
            Object cVal = e.getCVal();
            EntityType entity = e.getEntity();
            WriteBackFormula ruleItem = e.getRuleItem();
            Map<BFRowId, BigDecimal> srcRowVal = e.getSrcRowVal();
//            BigDecimal comt_onuse = srcRowVal.get("comt_onuse");
        }
    }
}

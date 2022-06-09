package comt.lam.formplugin;

import kd.bos.bill.AbstractBillPlugIn;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.metadata.IDataEntityProperty;
import kd.bos.dataentity.metadata.clr.DataEntityPropertyCollection;
import kd.bos.entity.datamodel.IEntryOperate;
import kd.bos.entity.property.BasedataProp;
import kd.bos.form.control.EntryGrid;
import kd.bos.form.control.events.RowClickEvent;
import kd.bos.form.control.events.RowClickEventListener;

import java.util.EventObject;
import java.util.HashMap;

public class assetsAssignedEdit extends AbstractBillPlugIn implements RowClickEventListener {
    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        EntryGrid assetsAppliedSubEntry = this.getView().getControl("comt_assigned_entry");
        assetsAppliedSubEntry.addRowClickListener(this);
    }

    @Override
    public void entryRowClick(RowClickEvent evt) {
        RowClickEventListener.super.entryRowClick(evt);
        SetHeadValue();
    }

    public void SetHeadValue() {
        IEntryOperate entryOperate = this.getModel();//把数据模型转换为单据体操作对象
        EntryGrid subEntryGrid = this.getView().getControl("comt_assigned_entry");//获取页面控件为单据表
        DynamicObjectCollection subEntryEntity = entryOperate.getEntryEntity("comt_assigned_entry");//根据获取子单据体实体
        int[] selectRows = subEntryGrid.getSelectRows();//获取子单据体所有选择行的下标

        if (selectRows.length != 1) {//选择行只能有一行
            this.getView().showErrorNotification("错误！请选择一行");
            return;
        }

        HashMap<String, String> KeyMap = new HashMap<String, String>() {//字段标识映射表
            {
                put("comt_name", "comt_basename");
                put("comt_size", "comt_basesize");
                put("comt_weight", "comt_baseweight");
                put("comt_unit", "comt_baseunit");
                put("comt_place", "comt_baseplace");
            }
        };

        DynamicObject singleRow = subEntryEntity.get(selectRows[0]);//获取唯一一行的行实体对象
        DataEntityPropertyCollection properties = singleRow.getDataEntityType().getProperties();//获取行的所有属性
        int parentsEntryRowIndex = entryOperate.getEntryCurrentRowIndex("comt_assetappliy_en");//使用单据体操作对象获取父母单据体当前选择的行号
        DynamicObject parentsObj = this.getModel().getEntryRowEntity("comt_assetappliy_en", parentsEntryRowIndex);//根据实体模型和父母单据体行号获取父母单据体的行实体
        DataEntityPropertyCollection parentsProperties = parentsObj.getDataEntityType().getProperties();//获取行实体的所有属性对象
        Object baseplace;

        for (IDataEntityProperty property : properties) {
            Object propertyValue = property.getValue(singleRow);
            String name = property.getName();
            String Key = KeyMap.get(name);
            if (Key != null) {
                this.getModel().setValue(Key, propertyValue);
            }
        }

        for (IDataEntityProperty parentsProperty : parentsProperties) {
            Object propertyValue = parentsProperty.getValue(parentsObj);
            String name = parentsProperty.getName();
            String Key = KeyMap.get(name);
            if (Key != null) {
                this.getModel().setValue(Key, propertyValue);
            }
            if (name != null && name.equals("comt_name") && propertyValue != null) {
                this.getModel().setValue("name", ((DynamicObject) propertyValue).get(2));
            }
        }

        baseplace = this.getModel().getValue("comt_baseplace");
        if (baseplace != null) {
            this.getModel().setValue("group", baseplace);
        }
    }
}

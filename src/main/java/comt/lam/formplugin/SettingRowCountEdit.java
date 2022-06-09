package comt.lam.formplugin;

import kd.bos.bill.AbstractBillPlugIn;
import kd.bos.bill.IBillPlugin;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.metadata.ICollectionProperty;
import kd.bos.dataentity.metadata.IDataEntityProperty;
import kd.bos.dataentity.metadata.IDataEntityType;
import kd.bos.dataentity.metadata.clr.ComplexProperty;
import kd.bos.dataentity.metadata.clr.DataEntityPropertyCollection;
import kd.bos.dataentity.utils.StringUtils;
import kd.bos.entity.MainEntityType;
import kd.bos.entity.datamodel.IEntryOperate;
import kd.bos.entity.property.BasedataProp;
import kd.bos.entity.property.EntryProp;
import kd.bos.form.control.EntryGrid;
import kd.bos.form.control.events.BeforeItemClickEvent;
import kd.bos.form.control.events.ItemClickEvent;
import kd.bos.form.field.BasedataEdit;
import kd.bos.form.field.events.AfterF7SelectEvent;
import kd.bos.form.field.events.AfterF7SelectListener;

import java.util.EventObject;
import java.util.HashMap;
import java.util.Set;

public class SettingRowCountEdit extends AbstractBillPlugIn implements IBillPlugin, AfterF7SelectListener {
    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        BasedataEdit edit = this.getView().getControl("comt_places");
        edit.addAfterF7SelectListener(this);
        this.addItemClickListeners();
    }

    public void beforeItemClick(BeforeItemClickEvent e) {
        if (StringUtils.equals("bar_save", e.getItemKey()) || StringUtils.equals("bar_modify", e.getItemKey()) || StringUtils.equals("bar_submitandnew", e.getItemKey()) || StringUtils.equals("bar_submit", e.getItemKey()) || StringUtils.equals("bar_audit", e.getItemKey())) {
            this.getView().showMessage("保存失败");
            e.setCancel(true);
        }
        DynamicObject dataEntity = this.getModel().getDataEntity(true);
        readPropValue(dataEntity);
    }

    @Override
    public void itemClick(ItemClickEvent evt) {
        super.itemClick(evt);
        if (StringUtils.equals("comt_subnew", evt.getItemKey())) {
            System.out.println("检测到子单据体增行按钮被点击");
            CheckValueUpdate();
        } else if (StringUtils.equals("comt_subdel", evt.getItemKey())) {
            System.out.println("检测到子单据体增行按钮被点击");
            CheckValueUpdate();
        }
    }

    @Override
    public void afterF7Select(AfterF7SelectEvent afterF7SelectEvent) {
        this.CheckValueUpdate();
    }

    private void CheckValueUpdate() {
//        this.getView().showMessage("触发了基础资料点击完成后事件！");
        DynamicObject dataEntity = this.getModel().getDataEntity(true);
        DynamicObjectCollection parentsObj = dataEntity.getDynamicObjectCollection("comt_assetaclloapp_en");
        for (DynamicObject parents : parentsObj) {
            int count = 0;
            DynamicObjectCollection subObj = parents.getDynamicObjectCollection("comt_subentry");
            HashMap<Long, Integer> map = new HashMap<>();
            for (DynamicObject sub : subObj) {
                if (sub.get("comt_places") != null) {
                    Integer times = map.get(sub);
                    int temp = sub.getInt("comt_stepper");
                    if (times == null)
                        times = temp;
                    else
                        times += temp;
                    Long id = (Long) sub.getDynamicObject("comt_places").getPkValue();
                    map.put(id, times);
                }
            }
            for (Integer item : map.values()) {
                if (map.size() != 0)
                    count += item;
            }
            System.out.println("第" + parentsObj.indexOf(parents) + "行有效数为" + count + "行");
            this.getModel().setValue("comt_applycount", count, parentsObj.indexOf(parents));
            if (map.size() != 0) {
                this.tidyUpRows(map, subObj);
            }
        }
    }

    private void tidyUpRows(HashMap<Long, Integer> map, DynamicObjectCollection subObj) {
        DynamicObjectCollection dynamicObjects = new DynamicObjectCollection();//临时存储行对象
        HashMap<Long, Integer> Count = new HashMap<>();//计算pkid出现次数
        MainEntityType mainEntityType = this.getModel().getDataEntityType();//获取主要实体类型
        BasedataProp baseDataProperty = (BasedataProp) mainEntityType.findProperty("comt_places");//根据基础资料标识在主实体中找到基础资料字段
        for (DynamicObject sub : subObj) {//遍历循环旧列表
            DynamicObject newRow = (DynamicObject) sub.getDataEntityType().createInstance();//复制当前行实体到新行
            DynamicObject refBaseDataObj;//基础资料对象
            DataEntityPropertyCollection rowProperties = newRow.getDataEntityType().getProperties();//获取新行的实体对象
            Long pkid = (Long) sub.getDynamicObject("comt_places").getPkValue();//获取当前行基础资料的pkid

            if (Count.get(pkid) == null) {//在哈希表中寻找pkid，如果没找到说明还未保存
                Count.put(pkid, 1);//设置计数为1
                refBaseDataObj = (DynamicObject) baseDataProperty.getValue(sub);//获取当前行的基础资料对象
                baseDataProperty.setValue(newRow, refBaseDataObj);//把当前行基础资料赋值到新行的基础资料中
                newRow.set("comt_stepper", 1);//把分配数量设置为1
                newRow.set("seq", Count.size());
                newRow.set("id", 0);
                newRow.set("comt_places_id", 0);
//                newRow.set("comt_places" , );
                dynamicObjects.add(newRow);//把新行添加到行临时存储对象中
            } else {//如果找到了哈希值，那么将哈希值加一，即分配数量加一
                int temp = Count.get(pkid);
                temp++;
                Count.put(pkid, temp);
                for (DynamicObject item : dynamicObjects) {//循环寻找pid相同的行，当pid相同时，则找到存储的行，把分配数量加1
                    Long id = (Long) item.getDynamicObject("comt_places").getPkValue();//获取某行的pid
                    if (pkid.compareTo(id) == 0)
                        item.set("comt_stepper", temp);
                }
            }
        }
        subObj.clear();//清空旧列表
        for (DynamicObject dynamicObject : dynamicObjects) {//把临时存储行的对象依次添加到旧列表中
            subObj.add(dynamicObject);
        }
    }

    private void readPropValue(DynamicObject dataEntity) {
        IDataEntityType dType = dataEntity.getDynamicObjectType();
        for (IDataEntityProperty property : dType.getProperties()) {
            if (property instanceof ICollectionProperty) {
                DynamicObjectCollection rows = dataEntity.getDynamicObjectCollection(property);
                for (DynamicObject row : rows) {
                    this.readPropValue(row);
                }
            } else if (property instanceof ComplexProperty) {
                DynamicObject refDataEntity = dataEntity.getDynamicObject(property);
                if (refDataEntity != null) {
                    this.readPropValue(refDataEntity);
                }
            } else {
                Object propValue = dataEntity.get(property);
                // 输出" 属性名 = 属性值 "
                String msg = String.format("%s = %s", property.getName(), propValue);
                System.out.println(msg);
            }
        }
    }
}

/*
  int arr[] = new int[subObj.size()];
              for (DynamicObject sub : subObj) {
                  int i = 0;
                  if (sub.get("comt_places") == null) {
                      if (subObj.indexOf(sub) > 0 && subObj.indexOf(sub) < subObj.size() - 1) {//不为第一个且不是最后一个存在空行的位置
                          arr[i] = subObj.indexOf(sub);//保存需要删除行的下标
                          i++;
                          flag = 1;
                      }
                      System.out.println("分配位置可能为空！");
                  } else if (sub.get("comt_places") != null) {
                      int temp = sub.getInt("comt_stepper");
                      tidyUpRows(sub);
                      System.out.println("分配位置在：" + sub.get("comt_places") + "分配量是：" + temp);
                      count += temp;
                  }
              }
              if (flag != 0) {
                  this.getModel().deleteEntryRows("comt_subentry", arr);
              }
 */

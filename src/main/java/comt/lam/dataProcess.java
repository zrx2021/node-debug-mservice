package comt.lam;

import kd.bos.bill.AbstractBillPlugIn;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.metadata.IDataEntityProperty;
import kd.bos.dataentity.metadata.clr.DataEntityPropertyCollection;
import kd.bos.dataentity.metadata.dynamicobject.DynamicComplexProperty;
import kd.bos.dataentity.metadata.dynamicobject.DynamicSimpleProperty;
import kd.bos.entity.MainEntityType;
import kd.bos.entity.SubEntryType;
import kd.bos.entity.property.BasedataProp;
import kd.bos.entity.property.DecimalProp;
import kd.bos.entity.property.FieldProp;
import kd.bos.entity.property.LongProp;

import java.math.BigDecimal;

public class dataProcess extends AbstractBillPlugIn {
    private static boolean hasEmpty = true;

    public boolean HasEmpty() {
        return hasEmpty;
    }

    private void setHasEmpty(boolean hasEmpty) {
        dataProcess.hasEmpty = hasEmpty;
    }

    public boolean CheckFailure(String... entryNames) {
        String s = CheckHead();
        if (s != null) {
            this.getView().showErrorNotification(s);
            setHasEmpty(true);
            return HasEmpty();
        }
        for (String entryName : entryNames) {
            s = CheckEntry(entryName);
            if (s != null) {
                this.getView().showErrorNotification(s);
                setHasEmpty(true);
                return HasEmpty();
            }
        }
        setHasEmpty(false);
        return HasEmpty();
    }

    public String CheckHead() {
        MainEntityType mainEntityType = this.getModel().getDataEntityType();
        DynamicObject dataEntity = this.getModel().getDataEntity();
        DataEntityPropertyCollection properties = mainEntityType.getProperties();
        for (IDataEntityProperty property : properties) {
            if (property instanceof FieldProp) {
                FieldProp fieldProp = (FieldProp) property;
                if (fieldProp.isMustInput()) {
                    if (dataEntity.get(property.getName()).toString() == null) {
                        return "单据头有空字段";
                    }
                }
            }
        }
        return null;
    }

    public String CheckEntry(String entryKey) {
        DynamicObjectCollection entryRowObjs = this.getModel().getEntryEntity(entryKey);
        if (entryRowObjs == null) {
            return "单据体为空！";
        }
        String entryDisplayName = String.valueOf(this.getModel().getProperty(entryKey).getDisplayName());
        DataEntityPropertyCollection properties = entryRowObjs.getDynamicObjectType().getProperties();
        for (DynamicObject entryRowObj : entryRowObjs) {
            int index = entryRowObjs.indexOf(entryRowObj);
            boolean hasParents = entryRowObj.getDataEntityType() instanceof SubEntryType;
            for (IDataEntityProperty property : properties) {
                String propertyName = property.getName();
                String displayName = String.valueOf(property.getDisplayName());
                Object propValue = entryRowObj.get(propertyName);
                if (property instanceof DynamicSimpleProperty) {
                    FieldProp fieldProp = (FieldProp) property;
                    if (fieldProp.isMustInput()) {
                        if (property instanceof DecimalProp || property instanceof LongProp) {//属性属于数值属性大全时成立
                            BigDecimal value = (BigDecimal) propValue;
                            if (value.compareTo(BigDecimal.valueOf(0)) == 0) {
                                return returnFormat(entryDisplayName, index, displayName, hasParents, false);
                            }
                        } else if (property instanceof FieldProp) {
                            if (propValue == null || propValue.equals("")) {
                                return returnFormat(entryDisplayName, index, displayName, hasParents, true);
                            }
                        }
                    }
                } else if (property instanceof DynamicComplexProperty) {
                    if (property instanceof BasedataProp) {
                        if (((BasedataProp) property).isMustInput() && propValue == null) {
                            return returnFormat(entryDisplayName, index, displayName, hasParents, true);
                        }
                    }
                }
            }
        }
        return null;
    }

    public String returnFormat(String entryDisplayName, int index, String propertyDisplayName, boolean hasParents, boolean isField) {
        index += 1;
        if (hasParents) {
            if (isField) {
                return String.format("子单据体 %s 的第 %d 行的 %s 字段的值为空", entryDisplayName, index, propertyDisplayName);
            } else {
                return String.format("子单据体 %s 的第 %d 行的 %s 字段的值为0", entryDisplayName, index, propertyDisplayName);
            }
        } else {
            if (isField) {
                return String.format("父母单据体 %s 的第 %d 行的 %s 字段的值为空\n如果有不需要分配的物件，请删除行", entryDisplayName, index, propertyDisplayName);
            } else {
                return String.format("父母单据体 %s 的第 %d 行的 %s 字段的值为0\n如果有不需要分配的物件，请删除行", entryDisplayName, index, propertyDisplayName);
            }
        }
    }
}

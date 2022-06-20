package comt.lam.bookmanage;

import comt.lam.dataProcess;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.entity.datamodel.IDataModel;
import kd.bos.entity.datamodel.events.ChangeData;
import kd.bos.entity.datamodel.events.PropertyChangedArgs;

import java.util.EventObject;

public class addNewBook extends dataProcess {
    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
    }

    @Override
    public void propertyChanged(PropertyChangedArgs e) {
        super.propertyChanged(e);
        if (!e.getProperty().getName().equals("comt_bookname")) {
            return;
        }
        ChangeData[] changeSet = e.getChangeSet();
        Object temp;
        for (ChangeData changeData : changeSet) {
            int rowIndex = changeData.getRowIndex();
            DynamicObject dataEntity = changeData.getDataEntity();
            IDataModel model = this.getModel();
            temp = dataEntity.get("comt_bookname.comt_price");
            model.setValue("comt_price", temp, rowIndex);
            temp = dataEntity.get("comt_bookname.comt_unit");
            model.setValue("comt_unit", temp, rowIndex);
        }
    }
}

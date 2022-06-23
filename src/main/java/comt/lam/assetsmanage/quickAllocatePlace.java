package comt.lam.assetsmanage;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import comt.lam.dataProcess;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.entity.datamodel.IDataModel;
import kd.bos.entity.datamodel.IEntryOperate;
import kd.bos.entity.datamodel.events.PropertyChangedArgs;
import kd.bos.form.FormShowParameter;
import kd.bos.form.events.AfterDoOperationEventArgs;
import kd.bos.form.events.BeforeDoOperationEventArgs;
import kd.bos.form.field.BasedataEdit;
import kd.bos.form.field.events.AfterF7SelectEvent;
import kd.bos.form.field.events.AfterF7SelectListener;
import kd.bos.form.operate.FormOperate;

import java.util.EventObject;
import java.util.Map;

public class quickAllocatePlace extends dataProcess implements AfterF7SelectListener {
    private static final String[] NOTIFICATION = {"无法整理地点，请选择一行物件", "错误！场地分配量总和大于可用数量，请仔细检查"};
    private static final String PARENTS_KEY = "comt_entry";
    private static final String SUB_KEY = "comt_subentry";
    private static final String BASE_DATA_KEY = "comt_place";
    private static final String STEPPER_KEY = "comt_stepper";
    private static final String AVAILABLE_KEY = "comt_available";

    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        BasedataEdit basedataEdit = this.getView().getControl("comt_place");
        basedataEdit.addAfterF7SelectListener(this);
    }

    @Override
    public void afterCreateNewData(EventObject e) {
        super.afterCreateNewData(e);
        IEntryOperate entryOperate = this.getModel();
        entryOperate.deleteEntryData("comt_entry");
        IDataModel model = this.getModel();
        FormShowParameter formShowParameter = this.getView().getFormShowParameter();
        Map<String, Object> params = formShowParameter.getCustomParams();
        JSONArray entry = (JSONArray) params.get("entry");

        for (Object row : entry) {
            JSONObject rowJSON = (JSONObject) row;
            int index = entryOperate.createNewEntryRow("comt_entry");
            JSONObject materiel = (JSONObject) rowJSON.get("comt_materiel");
            model.setValue("comt_materiel", materiel.get("id"), index);
            Object temp = rowJSON.get("comt_available");
            model.setValue("comt_available", temp, index);
        }
        this.getView().updateView("comt_entry");
    }

    @Override
    public void afterF7Select(AfterF7SelectEvent afterF7SelectEvent) {
        MergeSubEntry(PARENTS_KEY, SUB_KEY, AVAILABLE_KEY, BASE_DATA_KEY, STEPPER_KEY, NOTIFICATION);
        this.getView().updateView();
    }

    @Override
    public void beforeDoOperation(BeforeDoOperationEventArgs args) {
        super.beforeDoOperation(args);
        FormOperate source = (FormOperate) args.getSource();
        if ("confirm".equals(source.getOperateKey())) {
            this.getView().returnDataToParent("您点击了确定");
            DynamicObjectCollection entryEntity = this.getModel().getEntryEntity("comt_entry");
            this.getView().returnDataToParent(entryEntity);
            this.getView().close();
        } else if ("refresh".equals(source.getOperateKey())) {
            this.getView().updateView();
        }
    }

    @Override
    public void afterDoOperation(AfterDoOperationEventArgs afterDoOperationEventArgs) {
        super.afterDoOperation(afterDoOperationEventArgs);
    }

    @Override
    public void propertyChanged(PropertyChangedArgs e) {
        super.propertyChanged(e);
        System.out.println(e.getProperty().getName());
        if ("comt_stepper".equals(e.getProperty().getName())) {
            MergeSubEntry(PARENTS_KEY, SUB_KEY, AVAILABLE_KEY, BASE_DATA_KEY, STEPPER_KEY, NOTIFICATION);
            this.getView().updateView();
        }
    }

}

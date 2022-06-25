package comt.lam.bookmanage;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import comt.lam.dataProcess;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.entity.datamodel.IDataModel;
import kd.bos.entity.datamodel.IEntryOperate;
import kd.bos.form.FormShowParameter;
import kd.bos.form.IFormView;
import kd.bos.form.control.Control;
import kd.bos.form.events.AfterDoOperationEventArgs;
import kd.bos.form.events.BeforeDoOperationEventArgs;
import kd.bos.form.field.BasedataEdit;
import kd.bos.form.field.StepperEdit;
import kd.bos.form.field.events.AfterF7SelectEvent;
import kd.bos.form.field.events.AfterF7SelectListener;
import kd.bos.form.operate.FormOperate;

import java.util.EventObject;
import java.util.Map;

public class quickbook extends dataProcess implements AfterF7SelectListener {
    private static final String[] NOTIFICATION = {"无法整理详细位置，请选择一行书刊", "错误！位置分配量总和大于可新增数量，请仔细检查"};
    private static final String PARENTS_KEY = "comt_entry";
    private static final String SUB_KEY = "comt_subentry";
    private static final String BASE_DATA_KEY = "comt_detail";
    private static final String STEPPER_KEY = "comt_stepper";
    private static final String AVAILABLE_KEY = "comt_count";

    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        IFormView view = this.getView();
        BasedataEdit basedataEdit = view.getControl("comt_detail");
        basedataEdit.addAfterF7SelectListener(this);
        StepperEdit stepper = view.getControl("comt_stepper");
        stepper.addClickListener(this);
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
            JSONObject bookName = (JSONObject) rowJSON.get("comt_bookname");
            model.setValue("comt_bookname", bookName.get("id"), index);
            Object temp = rowJSON.get("comt_count");
            model.setValue("comt_count", temp, index);
        }
        this.getView().updateView("comt_entry");
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
    public void afterF7Select(AfterF7SelectEvent afterF7SelectEvent) {
        MergeSubEntry(PARENTS_KEY, SUB_KEY, AVAILABLE_KEY, BASE_DATA_KEY, STEPPER_KEY, NOTIFICATION);
        this.getView().updateView();
    }

    @Override
    public void afterDoOperation(AfterDoOperationEventArgs afterDoOperationEventArgs) {
        super.afterDoOperation(afterDoOperationEventArgs);
    }

    @Override
    public void click(EventObject evt) {
        super.click(evt);
        Control source = (Control) evt.getSource();
        if ("comt_stepper".equals(source.getKey())) {
            MergeSubEntry(PARENTS_KEY, SUB_KEY, AVAILABLE_KEY, BASE_DATA_KEY, STEPPER_KEY, NOTIFICATION);
            this.getView().updateView();
        }
    }
}

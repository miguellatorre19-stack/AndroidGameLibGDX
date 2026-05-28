package svalero.com.managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Disposable;

public class PopupMessageManager implements Disposable {
    private static final float PANEL_PADDING = 18f;
    private static final float MAX_WIDTH_FACTOR = 0.70f;
    private static final float FLOAT_DISTANCE_PX = 3f;
    private static final float FLOAT_TIME_SEC = 0.9f;

    private static class PopupRequest {
        String text;
        int[] dismissKeys;
    }

    private final Stage stage;
    private final BitmapFont font;
    private final Texture panelTexture;
    private final Table popupTable;
    private final Label messageLabel;
    private final Label dismissHintLabel;
    private final Array<PopupRequest> queue;
    private int[] currentDismissKeys;

    public PopupMessageManager(Stage stage) {
        this.stage = stage;
        this.queue = new Array<>();

        font = new BitmapFont();
        font.getData().setScale(1.1f);

        panelTexture = buildSolidTexture();

        popupTable = new Table();
        popupTable.setBackground(new TextureRegionDrawable(new TextureRegion(panelTexture)));
        popupTable.getColor().set(0f, 0f, 0f, 0.78f);
        popupTable.pad(PANEL_PADDING);

        Label.LabelStyle textStyle = new Label.LabelStyle(font, Color.valueOf("F2F2F2"));
        Label.LabelStyle hintStyle = new Label.LabelStyle(font, Color.valueOf("A8DADC"));

        messageLabel = new Label("", textStyle);
        messageLabel.setWrap(true);
        messageLabel.setAlignment(Align.center);

        dismissHintLabel = new Label("", hintStyle);
        dismissHintLabel.setAlignment(Align.center);

        popupTable.add(messageLabel).growX().center();
        popupTable.row();
        popupTable.add(dismissHintLabel).padTop(8f).center();
        popupTable.setVisible(false);

        stage.addActor(popupTable);
    }

    public void showMessage(String text) {
        showMessage(text, Input.Keys.E, Input.Keys.SPACE);
    }

    public void showMessage(String text, int... dismissKeys) {
        if (text == null) return;
        String normalized = text.trim();
        if (normalized.isEmpty()) return;

        int[] keys = normalizeDismissKeys(dismissKeys);
        PopupRequest request = new PopupRequest();
        request.text = normalized;
        request.dismissKeys = keys;

        if (isVisible()) {
            queue.add(request);
            return;
        }
        present(request);
    }

    public void update(float delta) {
        if (!isVisible()) return;
        if (wasDismissPressed()) {
            hideCurrent();
        }
    }

    public boolean isVisible() {
        return popupTable.isVisible();
    }

    public void clear() {
        popupTable.setVisible(false);
        popupTable.clearActions();
        queue.clear();
        currentDismissKeys = null;
    }

    public void onResize() {
        if (!isVisible()) return;
        relayoutCurrentPopup();
    }

    @Override
    public void dispose() {
        font.dispose();
        panelTexture.dispose();
    }

    private void present(PopupRequest request) {
        currentDismissKeys = request.dismissKeys;
        messageLabel.setText(request.text);
        dismissHintLabel.setText(buildDismissHint(currentDismissKeys));

        popupTable.setVisible(true);
        relayoutCurrentPopup();

        popupTable.clearActions();
        popupTable.addAction(
            Actions.forever(
                Actions.sequence(
                    Actions.moveBy(0f, FLOAT_DISTANCE_PX, FLOAT_TIME_SEC),
                    Actions.moveBy(0f, -FLOAT_DISTANCE_PX, FLOAT_TIME_SEC)
                )
            )
        );
    }

    private void hideCurrent() {
        popupTable.setVisible(false);
        popupTable.clearActions();
        if (queue.size > 0) {
            PopupRequest next = queue.removeIndex(0);
            present(next);
        }
    }

    private void relayoutCurrentPopup() {
        float worldW = stage.getViewport().getWorldWidth();
        float worldH = stage.getViewport().getWorldHeight();
        float popupW = worldW * MAX_WIDTH_FACTOR;

        messageLabel.setWidth(popupW - (PANEL_PADDING * 2f));
        dismissHintLabel.setWidth(popupW - (PANEL_PADDING * 2f));
        popupTable.setWidth(popupW);
        popupTable.pack();
        popupTable.setPosition((worldW - popupTable.getWidth()) * 0.5f, worldH * 0.72f);
    }

    private boolean wasDismissPressed() {
        for (int key : currentDismissKeys) {
            if (Gdx.input.isKeyJustPressed(key)) return true;
        }
        return false;
    }

    private int[] normalizeDismissKeys(int[] dismissKeys) {
        if (dismissKeys == null || dismissKeys.length == 0) {
            return new int[]{Input.Keys.E, Input.Keys.SPACE};
        }
        return dismissKeys;
    }

    private String buildDismissHint(int[] keys) {
        StringBuilder builder = new StringBuilder("Pulsa ");
        for (int i = 0; i < keys.length; i++) {
            if (i > 0) builder.append(" o ");
            builder.append(Input.Keys.toString(keys[i]).toUpperCase());
        }
        return builder.append(" para cerrar").toString();
    }

    private Texture buildSolidTexture() {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }
}

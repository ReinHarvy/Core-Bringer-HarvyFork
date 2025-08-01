package com.altf4studios.corebringer.screens;

import com.altf4studios.corebringer.Main;
import com.altf4studios.corebringer.utils.LoggingCollector;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;

public class DebugScreen implements Screen {
    private Main corebringer;
    private Stage coredebugscreenstage;
    private Table coredebugscreentable;
    private Table debugscreeninfotable;
    private Table debugscreenbuttons;
    private Label fpsdebug;
    private TextButton returntomainmenu;
    private TextButton reloadcardsbutton;
    private List<String> listofcards;
    private ScrollPane scrolllistofcards;
    private Array<String> carddescription;
    private Array<SampleCardHandler> loadedcards;
    private TextButton cardtestscreenbutton;
    private Table debugscreenjshelltable;
    private TextField jshellinput;
    private TextButton executebutton;
    private TextArea outputarea;
    private ScrollPane outputareascroll;
    private TextButton viewlogsbutton;

    public DebugScreen(Main corebringer) {
        this.corebringer = corebringer; ///The Master Key that holds all screens together

        ///Here's everything that will initiate upon doing the secret combo
        coredebugscreenstage = new Stage(new FitViewport(1280, 720));
        coredebugscreentable = new Table();
        coredebugscreentable.setFillParent(true);
        coredebugscreenstage.addActor(coredebugscreentable);

        ///Parameters for the Debug Info Table
        debugscreeninfotable = new Table();
        debugscreeninfotable.top().left().pad(10f);

        ///Parameters for the Buttons in the Debug Screen
        debugscreenbuttons = new Table();
        debugscreenbuttons.bottom().center().padBottom(20f);

        ///Parameters for the JShell Console Table in the Debug Screen
        debugscreenjshelltable = new Table();
        debugscreenjshelltable.bottom().center().padBottom(20f);

        ///Parameters for the FPS
        fpsdebug = new Label("FPS: ", corebringer.testskin);

        ///Parameters for the Return Button
        returntomainmenu = new TextButton("Return to Main Menu?", corebringer.testskin);

        ///This will give function to the Return Button
        returntomainmenu.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                corebringer.setScreen(corebringer.mainMenuScreen);
            }
        });

        ///This is the parameters of the Reload Cards button for the cards to be reloaded
        reloadcardsbutton = new TextButton("Reload Cards?", corebringer.testskin);

        ///This is for the cards list to be displayed
        listofcards = new List<>(corebringer.testskin);
        scrolllistofcards = new ScrollPane(listofcards, corebringer.testskin);
        scrolllistofcards.setFadeScrollBars(false);
        scrolllistofcards.setScrollingDisabled(true, false);
        scrolllistofcards.setForceScroll(false, true);

        ///This is for the function of the Reload Button
        reloadcardsbutton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                carddescription = new Array<>();
                loadedcards = new Array<>();
                try {
                    Json json = new Json();
                    JsonValue root = new JsonReader().parse(Gdx.files.internal("cards.json"));
                    for (JsonValue cardJson : root.get("cards")) {
                        SampleCardHandler cardHandler = json.readValue(SampleCardHandler.class, cardJson);
                        carddescription.add(cardHandler.toString());
                    }
                    listofcards.setItems(carddescription);
                } catch (Exception e) {
                    listofcards.setItems("Error loading cards: " + e.getMessage());
                }
            }
        });

        ///This is the button for the Card Test Screen
        cardtestscreenbutton = new TextButton("Move to Card Testing Screen?", corebringer.testskin);

        ///This is for the Card Test Screen button to be functional and to match the Card Handler
        cardtestscreenbutton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                String selected = listofcards.getSelected();
                if (selected != null) {
                    for(SampleCardHandler card : loadedcards) {
                        if (card.toString().equals(selected)) {
                            corebringer.selecteddebugcard = card;
                            break;
                        }
                    }
                }
                corebringer.setScreen(corebringer.cardTestScren);
            }
        });

        ///This is for implementing the JShell Snippet
        jshellinput = new TextField("", corebringer.testskin);
        executebutton = new TextButton("Run Input",corebringer.testskin);
        outputarea = new TextArea("", corebringer.testskin);
        viewlogsbutton = new TextButton("View Logs?", corebringer.testskin);
        outputarea.setDisabled(true);
        outputarea.setPrefRows(10);

        outputareascroll = new ScrollPane(outputarea, corebringer.testskin);
        outputareascroll.setFadeScrollBars(false);
        outputareascroll.setScrollingDisabled(true, false);
        outputareascroll.setForceScroll(false, true);

        ///This is to make the Execute Button functional
        executebutton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                String input = jshellinput.getText();
                String output = corebringer.evaluateJShellInput(input);

                outputarea.setText(output);
                outputarea.invalidateHierarchy();
                outputareascroll.layout();
            }
        });

        ///This is to make the View Logs button funtional
        viewlogsbutton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                String logs = LoggingCollector.getLogsAsString(); ///Importing the log history from Logging Collector
                outputarea.setText(logs.isEmpty() ? "No logs yet." : logs);
                outputarea.invalidateHierarchy();
                outputareascroll.layout();
            }
        });

        ///This is where the debug info, return button, and various other features will be added to tables
        debugscreeninfotable.add(fpsdebug);
        debugscreeninfotable.row().padTop(20f);
        debugscreeninfotable.add(scrolllistofcards).width(1200).height(200);
        debugscreenbuttons.add(reloadcardsbutton).padRight(20f);
        debugscreenbuttons.add(cardtestscreenbutton).padRight(20f);
        debugscreenbuttons.add(returntomainmenu);
        debugscreenjshelltable.add(outputareascroll).width(1200).height(200).colspan(2).padBottom(20f);
        debugscreenjshelltable.row().padTop(20f);
        debugscreenjshelltable.add(jshellinput).width(900).padRight(20f);
        debugscreenjshelltable.add(executebutton).padRight(10f);
        debugscreenjshelltable.row().padTop(20f);
        debugscreenjshelltable.add(viewlogsbutton);

        ///Table calling here since IDE reads code per line
        coredebugscreentable.add(debugscreeninfotable).expand().top().left().pad(10f);
        coredebugscreentable.row();
        coredebugscreentable.add(debugscreenjshelltable).expand().top().right().padBottom(20f);
        coredebugscreentable.row();
        coredebugscreentable.add(debugscreenbuttons).expand().bottom().center().padBottom(20f);
    }
    @Override
    public void show() {
        Gdx.input.setInputProcessor(coredebugscreenstage);
    }
    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);
        coredebugscreenstage.act(delta); ////Used to call the Stage and render the elements that is inside it
        coredebugscreenstage.draw();

        fpsdebug.setText("FPS: " + Gdx.graphics.getFramesPerSecond());
    }
    @Override public void resize(int width, int height) {
        coredebugscreenstage.getViewport().update(width, height, true);
        Gdx.input.setInputProcessor(coredebugscreenstage);
    }
    @Override public void pause() {

    }
    @Override public void resume() {

    }
    @Override public void hide() {
    }

    @Override
    public void dispose() {
        coredebugscreenstage.dispose();
    }
}

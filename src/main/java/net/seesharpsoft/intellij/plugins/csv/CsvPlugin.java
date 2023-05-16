package net.seesharpsoft.intellij.plugins.csv;

import consulo.application.dumb.DumbAware;
import consulo.application.progress.ProgressIndicator;
import consulo.application.progress.ProgressManager;
import consulo.application.progress.Task;
import consulo.project.Project;
import consulo.project.startup.BackgroundStartupActivity;
import consulo.ui.UIAccess;
import net.seesharpsoft.intellij.plugins.csv.components.CsvFileAttributes;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

public class CsvPlugin implements BackgroundStartupActivity, DumbAware {
    public static void doAsyncProjectMaintenance(@NotNull Project project) {
        ProgressManager.getInstance().run(new Task.Backgroundable(project, "CSV plugin validation"){
            public void run(@NotNull ProgressIndicator progressIndicator) {
                // initialize progress indication
                progressIndicator.setIndeterminate(false);

                // Set the progress bar percentage and text
                progressIndicator.setFraction(0.50);
                progressIndicator.setText("Validating CSV file attributes");

                // start process
                CsvFileAttributes csvFileAttributes = CsvFileAttributes.getInstance(project);
                csvFileAttributes.cleanupAttributeMap(project);

                // finished
                progressIndicator.setFraction(1.0);
                progressIndicator.setText("finished");
            }});
    }

    @Override
    public void runActivity(@Nonnull Project project, @Nonnull UIAccess uiAccess) {
        doAsyncProjectMaintenance(project);
    }
}
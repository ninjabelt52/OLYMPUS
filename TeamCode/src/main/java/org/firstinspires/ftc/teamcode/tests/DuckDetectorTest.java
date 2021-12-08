package org.firstinspires.ftc.teamcode.tests;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.classes.DuckDetectorPipelineBlue;
import org.firstinspires.ftc.teamcode.classes.DuckDetectorPipelineRed;


@TeleOp
public class DuckDetectorTest extends LinearOpMode {
    public void runOpMode(){
        DuckDetectorPipelineRed pipeline = new DuckDetectorPipelineRed(hardwareMap, "Webcam 1");

        while(!isStarted()){
            telemetry.addData("pipeline telemetry", pipeline);
            telemetry.update();
        }

        waitForStart();
        while (opModeIsActive()){
            telemetry.addData("pipeline telemetry", pipeline);
            telemetry.addData("estimated guess", pipeline.getPosition());
            telemetry.update();
        }
    }
}

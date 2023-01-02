package org.firstinspires.ftc.teamcode.Autos.BLUE;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.geometry.Vector2d;
import com.acmerobotics.roadrunner.trajectory.Trajectory;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

import org.firstinspires.ftc.teamcode.RoadRunner.drive.SampleMecanumDrive;
import org.firstinspires.ftc.teamcode.RoadRunner.trajectorysequence.TrajectorySequence;
import org.firstinspires.ftc.teamcode.classes.AutoAlignPipeline;
import org.firstinspires.ftc.teamcode.classes.LiftArm;
import org.firstinspires.ftc.teamcode.classes.MLToolChain;
import org.firstinspires.ftc.teamcode.classes.SignalSleeve;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

//-2.91
//-31.04
//3.65
@Autonomous
public class RightParkCone extends LinearOpMode {
    public void runOpMode() {
        AutoAlignPipeline.DuckPos sleevePos = AutoAlignPipeline.DuckPos.ONE;
        AutoAlignPipeline pipeline = new AutoAlignPipeline(hardwareMap, "Webcam 2");

        while(!pipeline.toString().equals("waiting for start")){
            telemetry.addLine("waiting for OpenCV");
            telemetry.update();
        }

        LiftArm lift = new LiftArm(hardwareMap);
        Thread liftThread = new Thread(lift);

        SampleMecanumDrive drive = new SampleMecanumDrive(hardwareMap);

        drive.setPoseEstimate(new Pose2d(-36,64.75, Math.toRadians(-90)));

        TrajectorySequence traj = drive.trajectorySequenceBuilder(drive.getPoseEstimate())
                .addTemporalMarker(0, () -> {
                    lift.setSlurpPower(1);
                    lift.lift(0, false);
                })
                .lineTo(new Vector2d(-36, -5))
                .lineTo(new Vector2d(-36, 12))
                .turn(Math.toRadians(-135))
                .addTemporalMarker(3, () -> {
                    pipeline.turnToAlign(.75, false);
                    lift.drop();
                })
                .build();

        while(!isStarted()) {
            if(gamepad1.a){
//                pipeline.frontCam.setPipeline(pipeline.poleDetector);
//                pipeline.backCam.setPipeline(pipeline.poleDetector);
            }else if(gamepad1.b){
//                pipeline.frontCam.setPipeline(pipeline.sleeveDetector);
//                pipeline.backCam.setPipeline(pipeline.sleeveDetector);
            }else if(gamepad1.y){
//                pipeline.frontCam.setPipeline(pipeline.sleeveDetector);
//                pipeline.backCam.setPipeline(pipeline.poleDetector);
            }

            sleevePos = pipeline.getSleevePosition();

            telemetry.addData("Sleeve position", pipeline.getSleevePosition());
            telemetry.addLine("waiting for start");
            telemetry.update();
        }

//        pipeline.frontCam.setPipeline(pipeline.poleDetector);

        waitForStart();
        liftThread.start();

        drive.followTrajectorySequence(traj);

        drive.update();

        Trajectory firstDeliver = drive.trajectoryBuilder(drive.getPoseEstimate())
                .addTemporalMarker(0, () -> {
                    lift.lift(1500,false);
                })
                .lineToLinearHeading(new Pose2d(drive.getPoseEstimate().getX() + Math.cos(45)*13, drive.getPoseEstimate().getY() - Math.sin(45)*13, drive.getPoseEstimate().getHeading()))
                .build();

        drive.followTrajectory(firstDeliver);

        lift.setSlurpPower(-1);
        sleep(500);

        for(int i = 0; i < 1; i++){
            TrajectorySequence pickup = drive.trajectorySequenceBuilder(drive.getPoseEstimate())
                    .addTemporalMarker(0, () -> {
                        lift.lift(0, true);
                        lift.setSlurpPower(0);
                    })
                    .lineToLinearHeading(new Pose2d(drive.getPoseEstimate().getX() - Math.cos(45)*13, drive.getPoseEstimate().getY() + Math.sin(45)*13, drive.getPoseEstimate().getHeading()))
                    .addTemporalMarker(1, () -> {
                        pipeline.strafeToAlign(.8, true);
                    })
                    .addTemporalMarker(2.5, () -> {
                        lift.lift(1000,true);
                        lift.setSlurpPower(1);
                    })
                    .turn(Math.toRadians(45))
                    .lineToLinearHeading(new Pose2d(-59,12, Math.toRadians(180)))
                    .build();

            drive.followTrajectorySequence(pickup);

            lift.drop(400);
            sleep(1000);
            lift.lift();

//            Trajectory deliver = drive.trajectoryBuilder(drive.getPoseEstimate(), true)
////                    .splineTo(new Vector2d(-48,12), Math.toRadians(180))
//                    .splineTo(new Vector2d(-36,12), Math.toRadians(-45))
//                    .build();

            TrajectorySequence deliver = drive.trajectorySequenceBuilder(drive.getPoseEstimate())
                    .addTemporalMarker(.5, () -> {
                        lift.drop();
                    })
                    .lineToLinearHeading(new Pose2d(-36,12, Math.toRadians(180)))
                    .turn(Math.toRadians(-45))
                    .build();

            drive.followTrajectorySequence(deliver);


            Trajectory backup = drive.trajectoryBuilder(drive.getPoseEstimate())
                    .addTemporalMarker(0, () -> {
                        lift.lift(1500, false);
                    })
                    .lineToLinearHeading(new Pose2d(drive.getPoseEstimate().getX() + Math.cos(45)*11, drive.getPoseEstimate().getY() - Math.sin(45)*11, drive.getPoseEstimate().getHeading()))
                    .build();

            drive.followTrajectory(backup);

            lift.setSlurpPower(-1);
        }

        TrajectorySequence park = drive.trajectorySequenceBuilder(drive.getPoseEstimate())
                .addTemporalMarker(1, () -> {
                    lift.setSlurpPower(0);
                    lift.lift(0, false);
                })
                .lineToLinearHeading(new Pose2d(drive.getPoseEstimate().getX() - Math.cos(45)*13, drive.getPoseEstimate().getY() + Math.sin(45)*13, drive.getPoseEstimate().getHeading()))
                .turn(Math.toRadians(-45))
                .lineToLinearHeading(new Pose2d(-36, 32, Math.toRadians(90)))
                .addTemporalMarker(2, () -> {
                    lift.drop();
                })
                .turn(Math.toRadians(-90))
                .build();

        drive.followTrajectorySequence(park);

        if(sleevePos.equals(AutoAlignPipeline.DuckPos.ONE)) {
            Trajectory trajL = drive.trajectoryBuilder(drive.getPoseEstimate())
                    .lineTo(new Vector2d(-14, 32))
                    .build();

            drive.followTrajectory(trajL);

            drive.turn(Math.toRadians(90));
        }else if(sleevePos.equals(AutoAlignPipeline.DuckPos.TWO)) {
        }else if(sleevePos.equals(AutoAlignPipeline.DuckPos.THREE)) {
            Trajectory trajR = drive.trajectoryBuilder(drive.getPoseEstimate())
                    .lineTo(new Vector2d(-60, 38))
                    .build();

            drive.followTrajectory(trajR);
        }

        try {
            File test = new File("/sdcard/FIRST/nums.txt");
            FileWriter writer = new FileWriter("/sdcard/FIRST/nums.txt");
            writer.write(Math.toDegrees(drive.getPoseEstimate().getHeading()) + "");
            writer.close();
            telemetry.addLine("successfully wrote!");
            telemetry.update();
        } catch (IOException e) {
            telemetry.addLine("couldn't create file");
            telemetry.update();
            e.printStackTrace();
        }

        liftThread.interrupt();
    }
}

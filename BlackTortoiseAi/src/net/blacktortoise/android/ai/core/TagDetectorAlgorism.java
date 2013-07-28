
package net.blacktortoise.android.ai.core;

import net.blacktortoise.android.ai.tagdetector.TagDetector;

import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;

public enum TagDetectorAlgorism {
    BRISK_BRISK, ORB_ORB;
    public static final TagDetectorAlgorism DEFAULT = BRISK_BRISK;

    public TagDetector createTagDetector() {
        switch (this) {
            case ORB_ORB:
                return new TagDetector( //
                        FeatureDetector.create(FeatureDetector.ORB), //
                        DescriptorExtractor.create(DescriptorExtractor.ORB), //
                        DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMINGLUT));
            case BRISK_BRISK:
            default:
                return new TagDetector( //
                        FeatureDetector.create(FeatureDetector.BRISK), //
                        DescriptorExtractor.create(DescriptorExtractor.BRISK), //
                        DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMINGLUT));
        }
    }
}

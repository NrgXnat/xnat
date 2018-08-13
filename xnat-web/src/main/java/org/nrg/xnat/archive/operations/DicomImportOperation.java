package org.nrg.xnat.archive.operations;

import org.nrg.framework.status.StatusProducerI;

import java.util.List;
import java.util.concurrent.Callable;

public interface DicomImportOperation extends Callable<List<String>>, StatusProducerI {
}

/*
 * Copyright (c) 2015-2017, Dell EMC
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.emc.metalnx.services.tests.tickets;

import com.emc.metalnx.core.domain.exceptions.*;
import com.emc.metalnx.services.interfaces.IRODSServices;
import com.emc.metalnx.services.interfaces.TicketClientService;
import org.apache.commons.io.FileUtils;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.Stream2StreamAO;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.ticket.packinstr.TicketCreateModeEnum;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

/**
 * Test iRODS services.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:test-services-context.xml")
@WebAppConfiguration
public class TestDownloadWithTicket {
    private static final String FILE_CONTENT = "Test for ticket";
    private static final int BUFFER_SIZE = 4 * 1024 * 1024;

    @Value("${irods.zoneName}")
    private String zone;

    @Value("${jobs.irods.username}")
    private String username;

    @Autowired
    private IRODSServices irodsServices;

    @Autowired
    private TicketClientService ticketClientService;

    private String targetPath, filePath, ticketString;
    private TestTicketUtils ticketUtils;
    private File localFile;

    @Before
    public void setUp() throws DataGridException, JargonException, IOException {
        String parentPath = String.format("/%s/home", zone);
        targetPath = String.format("%s/%s", parentPath, username);
        ticketUtils = new TestTicketUtils(irodsServices);
        localFile = ticketUtils.createLocalFile();
        ticketString = ticketUtils.createTicket(parentPath, username, TicketCreateModeEnum.READ);
        uploadFileToIRODS(targetPath, localFile);
        filePath = String.format("%s/%s", targetPath, localFile.getName());
    }

    @After
    public void tearDown() throws JargonException, DataGridConnectionRefusedException {
        FileUtils.deleteQuietly(localFile);
        ticketUtils.deleteTicket(ticketString);
        ticketUtils.deleteIRODSFile(filePath);
    }

    @Test
    public void testDownloadFileUsingATicket() throws DataGridMissingPathOnTicketException,
            DataGridConnectionRefusedException, DataGridNullTicketException, JargonException, IOException,
            DataGridTicketFileNotFound, DataGridTicketInvalidUser {
        File file = ticketClientService.getFileFromIRODSUsingTicket(ticketString, filePath);
        assertNotNull(file);
        assertEquals(TestTicketUtils.TICKET_FILE_CONTENT, FileUtils.readFileToString(file, StandardCharsets.UTF_8.name()));
    }

    private void uploadFileToIRODS(String path, File file) throws DataGridConnectionRefusedException, JargonException,
            IOException {
        IRODSFile targetFile = irodsServices.getIRODSFileFactory().instanceIRODSFile(path, file.getName());

        if (targetFile.exists()) {
            return;
        }

        Stream2StreamAO streamAO = irodsServices.getStream2StreamAO();
        InputStream inputStream = new FileInputStream(file);
        streamAO.transferStreamToFileUsingIOStreams(inputStream, (File) targetFile, 0, BUFFER_SIZE);
        inputStream.close();
        targetFile.close();
    }
}
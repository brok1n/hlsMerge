#include "mainwindow.h"
#include "ui_mainwindow.h"

#include "qurl.h"
#include "qbytearray.h"
#include "qfile.h"
#include "qprocess.h"
#include "qdir.h"
#include "qcryptographichash.h"
#include "qstandardpaths.h"

#include <QFileDialog>
#include <QByteArray>
#include <QtGlobal>
#include <QTableWidgetItem>
#include <QSslConfiguration>
#include <QSslSocket>
#include <QSsl>

#include <iostream>

MainWindow::MainWindow(QWidget *parent) :
    QMainWindow(parent),
    ui(new Ui::MainWindow)
{
    ui->setupUi(this);
    network  = new QNetworkAccessManager(this);
    connect(network, SIGNAL(finished(QNetworkReply*)), this, SLOT(networkStringFinish(QNetworkReply*)));
    download  = new QNetworkAccessManager(this);
    connect(download, SIGNAL(finished(QNetworkReply*)), this, SLOT(networkDownloadFinish(QNetworkReply*)));
    tsDownloadFinishCount = 0;

    ui->output_path_label->setText(QStandardPaths::writableLocation(QStandardPaths::DesktopLocation));
    outputPath = ui->output_path_label->text();

    QStringList header;
    header.append("CODE");
    header.append("URL");
    ui->tableWidget->setColumnCount(2);
    ui->tableWidget->setRowCount(0);
    ui->tableWidget->setColumnWidth(0, 50);
    ui->tableWidget->verticalHeader()->setVisible(false);
    ui->tableWidget->setHorizontalHeaderLabels(header);
    ui->tableWidget->horizontalHeader()->setStretchLastSection(true);
}

MainWindow::~MainWindow()
{
    delete ui;
}

void MainWindow::on_pushButton_clicked()
{
    //https://vid-egc.xnxx-cdn.com/videos/hls/22/b5/b6/22b5b6af5797396a6bfb9cb9b4a9f0b2/hls-360p.m3u8?fPGtfw6POuZLVwTgjXob0JLhkXAZhyo6pFQOCu3uvWA-dCPIuHdRC3jiP41DRmXK_d2HOwtVtPYTgKNIWq8PoSUVyN8gDDtVue99luMdv5IW96ONn90rKk9JBtp9ASF2lIAYIFZAtzqXbsXpsJ3OeGPHjA
    QString url = ui->lineEdit->text();
    url = url.trimmed();
    ui->lineEdit->setText(url);
    qDebug("url:%s ", qPrintable(url));
    tsDownloadFinishCount = 0;
    QByteArray urlMd5 = QCryptographicHash::hash(url.toLocal8Bit(), QCryptographicHash::Md5);
    dirName = urlMd5.toHex();

    QDir dir;
    if(!dir.exists(outputPath + QDir::separator() + dirName)){
       dir.mkdir(outputPath + QDir::separator() + dirName);
    }

    QFile file(outputPath + QDir::separator() + "m3u8list.txt");
    if (!file.open(QIODevice::Append)) {
      qDebug() << "can not open file:" << (outputPath + QDir::separator() + "m3u8list.txt");
      return;
    }
    file.write(url.toLatin1());
    file.write("\n");
    file.close();

    ui->progressBar->setValue(0);
    ui->progressBar->setMaximum(100);

    QNetworkRequest mRequest;
    QSslConfiguration config;
    config.setPeerVerifyMode(QSslSocket::VerifyNone);
    config.setProtocol(QSsl::SslV2);
    mRequest.setSslConfiguration(config);
    mRequest.setUrl(QUrl(url));

    network->get(mRequest);
//    network->get(QNetworkRequest(QUrl(url)));

    ui->plainTextEdit->clear();
    QStringList header;
    header.append("CODE");
    header.append("URL");
    ui->tableWidget->setColumnCount(2);
    ui->tableWidget->setRowCount(0);
    ui->tableWidget->setColumnWidth(0, 50);
    ui->tableWidget->verticalHeader()->setVisible(false);
    ui->tableWidget->setHorizontalHeaderLabels(header);
    ui->tableWidget->horizontalHeader()->setStretchLastSection(true);

    ui->plainTextEdit->appendPlainText("正在读取M3U8数据...");
    ui->pushButton->setEnabled(false);

}

void MainWindow::networkStringFinish(QNetworkReply *reply)
{
    int statusCode = reply->attribute(QNetworkRequest::HttpStatusCodeAttribute).toInt();
    qDebug() << "status code:" << statusCode;

    QVariant contentType = reply->header(QNetworkRequest::ContentTypeHeader);
    qDebug() << contentType.toString();
    //application/octet-stream

    if( statusCode == 200 ) {
        qDebug() << "请求成功！响应码:" << statusCode;
        QByteArray bytes = reply->readAll();
        QString data = bytes;

        QString filename = reply->url().fileName();
        QFile file(outputPath + QDir::separator() + dirName + QDir::separator() + filename);
        if (!file.open(QIODevice::WriteOnly)) {
          fprintf(stderr, "Could not open %s for writing: %s\n",
                  qPrintable(filename),
                  qPrintable(file.errorString()));
          return;
        }
        file.write(bytes);
        file.close();

        ui->plainTextEdit->appendPlainText("M3U8数据读取完毕 正在分析数据...");

        QStringList strList = data.split("\n");

        int tsFileCount = 0;

        tsUrlList.clear();

        QString downloadBaseUrl = reply->url().toString().left(reply->url().toString().indexOf(filename));
        for(int i = 0; i < strList.size(); i ++ ) {
            QString str = strList.at(i);
            if( str.startsWith("#EXTINF:") ) {
                i++;
                str = strList.at(i);
                if( str.trimmed().startsWith("http://") || str.trimmed().startsWith("https://")) {
                    tsUrlList.append(str);
                } else {
                    tsUrlList.append(downloadBaseUrl + str);
                }
                tsFileCount ++;
            }
        }

        ui->plainTextEdit->appendPlainText(tr("找到TS文件个数:%1").arg(tsFileCount));
        qDebug() << "找到TS文件个数:" << tsFileCount;
        qDebug() << "找到TS文件个数:" << tsUrlList.size();

        ui->plainTextEdit->appendPlainText("开始下载文件...");

        ui->progressBar->setMaximum(tsUrlList.size());


        if(tsUrlList.isEmpty()) {

            QString downloadBaseUrl = reply->url().toString().left(reply->url().toString().indexOf(filename));
            QString newUrl = "";
            for(int i = 0; i < strList.size(); i ++ ) {
                QString str = strList.at(i);
                if( str.startsWith("#EXT-X-STREAM-INF:") ) {
                    i++;
                    str = strList.at(i);
                    if( str.trimmed().startsWith("http://") || str.trimmed().startsWith("https://")) {
                        newUrl = str;
                    } else {
                        newUrl = downloadBaseUrl + str;
                    }
                }
            }

            if( newUrl.length() < 5 ) {
                 ui->pushButton->setEnabled(true);
            } else {
                //重新根据这个M3U8地址下载
                ui->plainTextEdit->appendPlainText(tr("正在重定向到:").append(newUrl));
                network->get(QNetworkRequest(QUrl(newUrl)));
                return ;
            }
        }

        for( int i = 0; i < tsUrlList.size(); i ++ ) {
            download->get(QNetworkRequest(QUrl(tsUrlList.at(i))));
        }
    } else {
        ui->plainTextEdit->appendPlainText(tr("请求失败！响应码:%1").arg(statusCode));
        ui->pushButton->setEnabled(true);
    }

}

//https://vid-egc.xnxx-cdn.com/videos/hls/22/b5/b6/22b5b6af5797396a6bfb9cb9b4a9f0b2/hls-360p34.ts?fPGtfw6POuZLVwTgjXob0JLhkXAZhyo6pFQOCu3uvWA-dCPIuHdRC3jiP41DRmXK_d2HOwtVtPYTgKNIWq8PoSUVyN8gDDtVue99luMdv5IW96ONn90rKk9JBtp9ASF2lIAYIFZAtzqXbsXpsJ3OeGPHjA

void MainWindow::networkDownloadFinish(QNetworkReply *reply)
{
    int statusCode = reply->attribute(QNetworkRequest::HttpStatusCodeAttribute).toInt();
    if( statusCode != 200 ) {
        qDebug() << "download error statusCode:" << statusCode;
        return ;
    }

    QString filename = reply->url().fileName();
    QFile file(outputPath + QDir::separator() + dirName + QDir::separator() + filename);
    if (!file.open(QIODevice::WriteOnly)) {
//      fprintf(stderr, "Could not open %s for writing: %s\n",
//              qPrintable(filename),
//              qPrintable(file.errorString()));
      return;
    }
    file.write(reply->readAll());
    file.close();

    ui->tableWidget->setRowCount(tsDownloadFinishCount+1);
    ui->tableWidget->setItem(tsDownloadFinishCount, 0, new QTableWidgetItem(tr("%1").arg(statusCode)));
    ui->tableWidget->setItem(tsDownloadFinishCount, 1,new QTableWidgetItem(reply->url().toString()));


    tsDownloadFinishCount ++;
    ui->progressBar->setValue(tsDownloadFinishCount);

    ui->plainTextEdit->appendPlainText(tr("文件下载结束:").append(reply->url().toString()));
    ui->plainTextEdit->appendPlainText(tr("已下载:%1 未下载:%2").arg(tsDownloadFinishCount).arg(tsUrlList.size() - tsDownloadFinishCount));

    if(tsDownloadFinishCount == tsUrlList.size()) {
        ui->plainTextEdit->appendPlainText(tr("TS列表下载完毕！共下载:%1个TS文件").arg(tsDownloadFinishCount));

        ui->plainTextEdit->appendPlainText("开始合并TS文件，请稍后...");

        std::string cmdStr =  " cd " + outputPath.toStdString() + QDir::separator().toLatin1() + dirName.toStdString() + QDir::separator().toLatin1() + " && " + workDir.toStdString() + QDir::separator().toLatin1() + "ffmpeg -i \"concat:";

        std::string args = "";

        for(int i = 0; i < tsUrlList.size(); i ++ ){
            std::string name = QUrl(tsUrlList.at(i)).fileName().toStdString();
            args.append(name);
            if(i < tsUrlList.size() - 1 )
                args.append("|");
        }
        args.append("\"");

        cmdStr.append(args);
        cmdStr.append(" -c copy output_all.ts");

        //Q_OS_LINUX
        //Q_OS_MACOS
        #ifdef Q_OS_WIN
            cmdStr = outputPath.left(1).toStdString().append(": && ").append(cmdStr);
        #endif

        system(cmdStr.c_str());

        qDebug() << "fileArg:" << QString::fromStdString(args);
        qDebug() << "cmdStr:" << QString::fromStdString(cmdStr);

        ui->plainTextEdit->appendPlainText(QString::fromStdString(cmdStr));

        //ui->plainTextEdit->appendPlainText(tr("ffmpeg -i ").append(fileArgs).append(" -c copy ").append(dirName).append(".ts"));

        QFile file1(outputPath + QDir::separator() + dirName + QDir::separator() + "merge.sh");
        if (!file1.open(QIODevice::WriteOnly)) {
          return;
        }

        file1.write("#!/bin/bash\n");
        file1.write( QString::fromStdString(cmdStr).toLocal8Bit());
        file1.write("\n");
        file1.close();

        ui->plainTextEdit->appendPlainText(outputPath + QDir::separator() + dirName + QDir::separator());

        ui->plainTextEdit->appendPlainText("文件合并完毕");

        ui->pushButton->setEnabled(true);

    }

}

void MainWindow::on_pushButton_2_clicked()
{
    ui->plainTextEdit->appendPlainText(tr("TS列表下载完毕！共下载:%1个TS文件").arg(tsDownloadFinishCount));

    ui->plainTextEdit->appendPlainText("开始合并TS文件，请稍后...");

    std::string cmdStr =  " cd " + outputPath.toStdString() + QDir::separator().toLatin1() + dirName.toStdString() + QDir::separator().toLatin1() + " && " + workDir.toStdString() + QDir::separator().toLatin1() + "ffmpeg -i \"concat:";

    std::string args = "";

    for(int i = 0; i < tsUrlList.size(); i ++ ){
        std::string name = QUrl(tsUrlList.at(i)).fileName().toStdString();
        args.append(name);
        if(i < tsUrlList.size() - 1 )
            args.append("|");
    }
    args.append("\"");

    cmdStr.append(args);
    cmdStr.append(" -c copy output_all.ts");

    //Q_OS_LINUX
    //Q_OS_MACOS
    #ifdef Q_OS_WIN
        cmdStr = outputPath.left(1).toStdString().append(": && ").append(cmdStr);
    #endif

    system(cmdStr.c_str());

    qDebug() << "fileArg:" << QString::fromStdString(args);
    qDebug() << "cmdStr:" << QString::fromStdString(cmdStr);

    ui->plainTextEdit->appendPlainText(QString::fromStdString(cmdStr));

    //ui->plainTextEdit->appendPlainText(tr("ffmpeg -i ").append(fileArgs).append(" -c copy ").append(dirName).append(".ts"));

    QFile file1(outputPath + QDir::separator() + dirName + QDir::separator() + "merge.sh");
    if (!file1.open(QIODevice::WriteOnly)) {
      return;
    }

    file1.write("#!/bin/bash\n");
    file1.write( QString::fromStdString(cmdStr).toLocal8Bit());
    file1.write("\n");
    file1.close();

    ui->plainTextEdit->appendPlainText(outputPath + QDir::separator() + dirName + QDir::separator());

    ui->plainTextEdit->appendPlainText("文件合并完毕");

    ui->pushButton->setEnabled(true);

}

void MainWindow::on_pushButton_3_clicked()
{

    outputPath = QFileDialog::getExistingDirectory(this,tr("选择输出目录"), outputPath);
    qDebug("select output path:%s", qPrintable(outputPath));
    ui->output_path_label->setText(outputPath);
}

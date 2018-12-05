#ifndef MAINWINDOW_H
#define MAINWINDOW_H

#include <QMainWindow>
#include "qnetworkaccessmanager.h"
#include "qnetworkrequest.h"
#include "qnetworkreply.h"

namespace Ui {
class MainWindow;
}

class MainWindow : public QMainWindow
{
    Q_OBJECT

public:
    explicit MainWindow(QWidget *parent = 0);
    ~MainWindow();

private slots:
    void on_pushButton_clicked();
    void networkStringFinish(QNetworkReply *);
    void networkDownloadFinish(QNetworkReply *);

    void on_pushButton_2_clicked();

    void on_pushButton_3_clicked();

private:
    Ui::MainWindow *ui;
    QNetworkAccessManager *network;
    QNetworkAccessManager *download;
    QStringList tsUrlList;
    int tsDownloadFinishCount;
    QString dirName;
    QString outputPath;
    QString m3u8Url;

public:
    QString workDir;


};

#endif // MAINWINDOW_H
